package sh.minty.helixis.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.Callable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
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
    private static final int KEY_SIZE = 128; // in bits
    private static final int IV_SIZE = 16; // in bytes
    SecureRandom secureRandom = new SecureRandom();

    @Override
    public Integer call() {
        try {
            var secretKey = deriveKey(password);
            var cipher = Cipher.getInstance(TRANSFORMATION);

            if ("encrypt".equalsIgnoreCase(mode)) {
                if (isFile) {
                    encryptFile(cipher, secretKey, new File(input), outputFile);
                } else {
                    var encryptedText = encryptString(cipher, secretKey, input);
                    System.out.println("Encrypted: " + encryptedText);
                }
            } else if ("decrypt".equalsIgnoreCase(mode)) {
                if (isFile) {
                    decryptFile(cipher, secretKey, new File(input), outputFile);
                }
                else {
                    var decryptedText = decryptString(cipher, secretKey, input);
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

    private SecretKey deriveKey(String password) throws NoSuchAlgorithmException {
        // TODO: use PBKDF2 instead of a direct key from password bytes
        byte[] keyBytes = password.getBytes(StandardCharsets.UTF_8);

        // pad or truncate keyBytes to match KEY_SIZE (16 bytes for 128-bit AES)
        byte[] finalKeyBytes = new byte[KEY_SIZE / 8];
        System.arraycopy(keyBytes, 0, finalKeyBytes, 0, Math.min(keyBytes.length, finalKeyBytes.length));

        return new SecretKeySpec(finalKeyBytes, ALGORITHM);
    }

    private String encryptString(Cipher cipher, SecretKey secretKey, String plainText) throws Exception {
        byte[] iv = new byte[IV_SIZE];
        secureRandom.nextBytes(iv);
        var ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // prepend IV to the encrypted data for decryption
        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    private String decryptString(Cipher cipher, SecretKey secretKey, String encryptedText) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedText);

        var iv = new byte[IV_SIZE];
        System.arraycopy(combined, 0, iv, 0, IV_SIZE);
        var ivSpec = new IvParameterSpec(iv);

        byte[] encryptedBytes = new byte[combined.length - IV_SIZE];
        System.arraycopy(combined, IV_SIZE, encryptedBytes, 0, encryptedBytes.length);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private void encryptFile(Cipher cipher, SecretKey secretKey, File inputFile, File outputFile) throws Exception {
        if (!inputFile.exists()) {
            System.err.println("Error: Input file not found at " + inputFile.getAbsolutePath());
            return;
        }
        if (outputFile == null) {
            System.err.println("Error: Output file path is required for file encryption.");
            return;
        }

        byte[] iv = new byte[IV_SIZE];
        secureRandom.nextBytes(iv);
        var ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        try (var fis = new FileInputStream(inputFile); var fos = new FileOutputStream(outputFile)) {

            fos.write(iv); // Write IV to the beginning of the output file

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    fos.write(output);
                }
            }
            byte[] output = cipher.doFinal();
            if (output != null) {
                fos.write(output);
            }
            System.out.println("File encrypted successfully to: " + outputFile.getAbsolutePath());
        }
    }

    private void decryptFile(Cipher cipher, SecretKey secretKey, File inputFile, File outputFile) throws Exception {
        if (!inputFile.exists()) {
            System.err.println("Error: Input file not found at " + inputFile.getAbsolutePath());
            return;
        }
        if (outputFile == null) {
            System.err.println("Error: Output file path is required for file decryption.");
            return;
        }

        try (var fis = new FileInputStream(inputFile); var fos = new FileOutputStream(outputFile)) {
            byte[] iv = new byte[IV_SIZE];
            if (fis.read(iv) != IV_SIZE) {
                throw new IOException("Could not read IV from encrypted file.");
            }
            var ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    fos.write(output);
                }
            }
            byte[] output = cipher.doFinal();
            if (output != null) {
                fos.write(output);
            }
            System.out.println("File decrypted successfully to: " + outputFile.getAbsolutePath());
        }
    }
}
