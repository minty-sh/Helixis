package sh.minty.helixis.commands;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "checksum", mixinStandardHelpOptions = true, description = "Calculates file checksums (SHA-256, MD5, SHA-1, SHA-512).", subcommands = {
        ChecksumCommand.SHA256Command.class, ChecksumCommand.MD5Command.class, ChecksumCommand.SHA1Command.class,
        ChecksumCommand.SHA512Command.class})
public class ChecksumCommand {

    private static String calculateChecksum(Path filePath, String algorithm)
            throws IOException, NoSuchAlgorithmException {
        var digest = MessageDigest.getInstance(algorithm);

        try (var fis = new FileInputStream(filePath.toFile())) {
            var byteArray = new byte[1024];
            int bytesCount = 0;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }

        var bytes = digest.digest();
        var sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    abstract static class BaseChecksumCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The path to the file.")
        protected String filePathString;

        protected abstract String getAlgorithm();

        @Override
        public Integer call() {
            Path filePath = Paths.get(filePathString);
            if (!filePath.toFile().exists()) {
                System.err.println("Error: File not found: " + filePathString);
                return 1;
            }
            try {
                var checksum = calculateChecksum(filePath, getAlgorithm());
                System.out.println(checksum);
                return 0;
            } catch (NoSuchAlgorithmException e) {
                System.err.println("Error: Algorithm not found: " + getAlgorithm() + ". " + e.getMessage());
                return 1;
            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "sha256", mixinStandardHelpOptions = true, description = "Calculates the SHA-256 checksum of a file.")
    static class SHA256Command extends BaseChecksumCommand {
        @Override
        protected String getAlgorithm() {
            return "SHA-256";
        }
    }

    @Command(name = "md5", mixinStandardHelpOptions = true, description = "Calculates the MD5 checksum of a file.")
    static class MD5Command extends BaseChecksumCommand {
        @Override
        protected String getAlgorithm() {
            return "MD5";
        }
    }

    @Command(name = "sha1", mixinStandardHelpOptions = true, description = "Calculates the SHA-1 checksum of a file.")
    static class SHA1Command extends BaseChecksumCommand {
        @Override
        protected String getAlgorithm() {
            return "SHA-1";
        }
    }

    @Command(name = "sha512", mixinStandardHelpOptions = true, description = "Calculates the SHA-512 checksum of a file.")
    static class SHA512Command extends BaseChecksumCommand {
        @Override
        protected String getAlgorithm() {
            return "SHA-512";
        }
    }
}
