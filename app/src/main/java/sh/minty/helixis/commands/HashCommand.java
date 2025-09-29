package sh.minty.helixis.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;

@Command(name = "hash", mixinStandardHelpOptions = true, description = "Calculates the hash of a string or a file.")
public class HashCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The input string or file path to hash.")
    private String input;

    @Option(
        names = {"-a", "--algorithm"},
        description = "Hashing algorithm (e.g., SHA-256, SHA-512). Default: SHA-256",
        defaultValue = "SHA-256"
    )
    private String algorithm;

    @Option(names = {"-f", "--file"}, description = "Treat input as a file path.")
    private boolean isFile;

    @Override
    public Integer call() {
        try {
            var digest = MessageDigest.getInstance(algorithm);
            byte[] hashedBytes;

            if (isFile) {
                File file = new File(input);
                if (!file.exists()) {
                    System.err.println("Error: File not found at " + input);
                    return 1;
                }
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        digest.update(buffer, 0, bytesRead);
                    }
                }
                hashedBytes = digest.digest();
            } else {
                hashedBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            }

            System.out.println("Hash (" + algorithm + "): " + bytesToHex(hashedBytes));
            return 0;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error: Invalid hashing algorithm specified: " + algorithm);
            return 1;
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return 1;
        }
    }

    private String bytesToHex(byte[] hash) {
        var hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
