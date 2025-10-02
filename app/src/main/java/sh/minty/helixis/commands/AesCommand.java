package sh.minty.helixis.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.Callable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

@Command(
    name = "aes", mixinStandardHelpOptions = true,
    description = "Encrypts or decrypts strings or files using AES."
)
public class AesCommand implements Callable<Integer> {
    @Parameters(index = "0", description = "Mode: 'encrypt' or 'decrypt'")
    private String mode;

    @Parameters(index = "1", description = "Input string or file path.")
    private String input;

    @Option(names = {"-p", "--password"}, description = "Password for key derivation.", required = true)
    private String password;

    @Option(names = {"-o", "--output"}, description = "Output file path (for file operations).")
    private File outputFile;

    @Option(names = {"-f", "--file"}, description = "Treat input as a file path.")
    private boolean isFile;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 128; // bits
    private static final int IV_SIZE = 16; // bytes (AES block size)
    private static final int SALT_SIZE = 16; // bytes
    private static final int PBKDF2_ITERATIONS = 65536;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public Integer call() {
        try {
            var cipher = Cipher.getInstance(TRANSFORMATION);

            if ("encrypt".equalsIgnoreCase(mode)) {
                if (isFile) {
                    encryptFile(cipher, password, new File(input), outputFile);
                } else {
                    var encryptedText = encryptString(cipher, password, input);
                    System.out.println("Encrypted: " + encryptedText);
                }
            } else if ("decrypt".equalsIgnoreCase(mode)) {
                if (isFile) {
                    decryptFile(cipher, password, new File(input), outputFile);
                } else {
                    var decryptedText = decryptString(cipher, password, input);
                    System.out.println("Decrypted: " + decryptedText);
                }
            } else {
                System.err.println("Invalid mode. Use 'encrypt' or 'decrypt'.");
                return 1;
            }
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    private SecretKey deriveKey(String password, byte[] salt) throws Exception {
        var factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        var spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE);
        var tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), ALGORITHM);
    }

    private String encryptString(Cipher cipher, String password, String plainText) throws Exception {
        // generate salt
        byte[] salt = new byte[SALT_SIZE];
        secureRandom.nextBytes(salt);

        // derive key
        var secretKey = deriveKey(password, salt);

        // generate IV
        byte[] iv = new byte[IV_SIZE];
        secureRandom.nextBytes(iv);
        var ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // combined: salt || iv || ciphertext
        byte[] combined = new byte[salt.length + iv.length + encryptedBytes.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(iv, 0, combined, salt.length, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, salt.length + iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    private String decryptString(Cipher cipher, String password, String encryptedText) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedText);

        if (combined.length < SALT_SIZE + IV_SIZE + 1) {
            throw new IllegalArgumentException("Encrypted data too short (missing salt/iv/ciphertext).");
        }

        byte[] salt = new byte[SALT_SIZE];
        System.arraycopy(combined, 0, salt, 0, salt.length);

        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(combined, SALT_SIZE, iv, 0, IV_SIZE);
        var ivSpec = new IvParameterSpec(iv);

        byte[] encryptedBytes = new byte[combined.length - SALT_SIZE - IV_SIZE];
        System.arraycopy(combined, SALT_SIZE + IV_SIZE, encryptedBytes, 0, encryptedBytes.length);

        var secretKey = deriveKey(password, salt);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private void encryptFile(Cipher cipher, String password, File inputFile, File outputFile) throws Exception {
        if (!inputFile.exists()) {
            System.err.println("Error: Input file not found at " + inputFile.getAbsolutePath());
            return;
        }
        if (outputFile == null) {
            System.err.println("Error: Output file path is required for file encryption.");
            return;
        }

        byte[] salt = new byte[SALT_SIZE];
        secureRandom.nextBytes(salt);
        var secretKey = deriveKey(password, salt);

        byte[] iv = new byte[IV_SIZE];
        secureRandom.nextBytes(iv);
        var ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        try (var fis = new FileInputStream(inputFile); var fos = new FileOutputStream(outputFile)) {
            // write salt and iv first
            fos.write(salt);
            fos.write(iv);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] out = cipher.update(buffer, 0, bytesRead);
                if (out != null) {
                    fos.write(out);
                }
            }
            byte[] out = cipher.doFinal();
            if (out != null) {
                fos.write(out);
            }
            System.out.println("File encrypted successfully to: " + outputFile.getAbsolutePath());
        }
    }

    private void decryptFile(Cipher cipher, String password, File inputFile, File outputFile) throws Exception {
        if (!inputFile.exists()) {
            System.err.println("Error: Input file not found at " + inputFile.getAbsolutePath());
            return;
        }
        if (outputFile == null) {
            System.err.println("Error: Output file path is required for file decryption.");
            return;
        }

        try (var fis = new FileInputStream(inputFile); var fos = new FileOutputStream(outputFile)) {
            byte[] salt = new byte[SALT_SIZE];
            if (fis.read(salt) != SALT_SIZE) {
                throw new IOException("Could not read salt from encrypted file.");
            }

            byte[] iv = new byte[IV_SIZE];
            if (fis.read(iv) != IV_SIZE) {
                throw new IOException("Could not read IV from encrypted file.");
            }
            var ivSpec = new IvParameterSpec(iv);

            var secretKey = deriveKey(password, salt);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] out = cipher.update(buffer, 0, bytesRead);
                if (out != null) {
                    fos.write(out);
                }
            }
            byte[] out = cipher.doFinal();
            if (out != null) {
                fos.write(out);
            }
            System.out.println("File decrypted successfully to: " + outputFile.getAbsolutePath());
        }
    }
}

