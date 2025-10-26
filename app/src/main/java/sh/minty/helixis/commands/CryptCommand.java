package sh.minty.helixis.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "crypt", mixinStandardHelpOptions = true, description = "Cryptography operations.", subcommands = {
        CryptCommand.ReverseAlphabet.class, HashCommand.class, AesCommand.class})
public class CryptCommand {
    @Command(name = "reverse-alphabet", mixinStandardHelpOptions = true, description = "Encrypts or decrypts a file using a reverse alphabet cipher.")
    static class ReverseAlphabet implements Callable<Integer> {
        @Parameters(index = "0", description = "Mode: 'encrypt' or 'decrypt'")
        private String mode;

        @Parameters(index = "1", description = "Path to the input file")
        private File inputFile;

        private static final Map<Character, Character> REVERSE_ALPHABET = new HashMap<>();

        static {
            for (char c = 'a'; c <= 'z'; c++) {
                REVERSE_ALPHABET.put(c, (char) ('a' + 'z' - c));
            }
        }

        @Override
        public Integer call() throws IOException {
            if (!"encrypt".equalsIgnoreCase(mode) && !"decrypt".equalsIgnoreCase(mode)) {
                System.err.println("Invalid mode. Use 'encrypt' or 'decrypt'.");
                return 1;
            }

            if (!inputFile.exists()) {
                System.err.println("The file doesn't exist.");
                return 1;
            }

            var content = new String(Files.readAllBytes(inputFile.toPath()));
            String result;

            if ("encrypt".equalsIgnoreCase(mode)) {
                System.out.println("Encrypted text:");
                result = transformMessage(content);
            } else { // decrypt
                System.out.println("Decrypted text:");
                result = transformMessage(content);
            }

            System.out.println(result);
            return 0;
        }

        private String transformMessage(String input) {
            var output = new StringBuilder();
            for (char c : input.toCharArray()) {
                if (Character.isLetter(c)) {
                    char lowerC = Character.toLowerCase(c);
                    char encryptedChar = REVERSE_ALPHABET.get(lowerC);
                    output.append(Character.isUpperCase(c) ? Character.toUpperCase(encryptedChar) : encryptedChar);
                } else {
                    output.append(c);
                }
            }
            return output.toString();
        }
    }
}
