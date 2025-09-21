package sh.minty.helixis.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

// TODO: come up with better names for these variables
record GenerationParams(int length, char separator, int separators) {}

@Command(name = "password", mixinStandardHelpOptions = true, description = "Generate X number of random passwords.")
public class PasswordCommand implements Callable<Integer> {
    private static final Logger LOGGER = Logger.getLogger(PasswordCommand.class.getName());
    private SecureRandom random = new SecureRandom();

    @Option(names = {"-n", "--number"}, description = "generate x passwords (default: 1)")
    private int numPasswords = 1;

    @Option(names = {"-f", "--file"}, description = "write passwords to filename")
    private String filename;

    @Override
    public Integer call() {
        if (numPasswords <= 0) {
            System.err.println("Error: Invalid number of passwords.");
            return 1;
        }

        List<String> passwords = new ArrayList<>();
        for (int i = 0; i < numPasswords; i++) {
            passwords.add(generatePassword());
        }

        if (filename != null) {
            var f = new File(filename);
            if (!isValidFilename(f.getName())) {
                System.err.println("Invalid argument error while writing passwords: Invalid filename!");
                return 1;
            }
            try (var writer = new FileWriter(f)) {
                for (var password : passwords) {
                    writer.write(password + System.lineSeparator());
                }
                LOGGER.info("File written!");
            } catch (IOException e) {
                System.err.println("Runtime error while writing passwords: " + e.getMessage());
                return 1;
            }
        } else {
            for (var password : passwords) {
                System.out.println(password);
            }
        }

        return 0;
    }

    private String generatePassword(String format, GenerationParams params) {
        final String lowercaseLetters = "abcdefghijklmnopqrstuvwxyz";
        
        char uppercaseLetter = Character.toUpperCase(lowercaseLetters.charAt(random.nextInt(lowercaseLetters.length())));
        char number = (char) ('0' + random.nextInt(10));

        var letters = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            letters.append(lowercaseLetters.charAt(random.nextInt(lowercaseLetters.length())));
        }

        letters.insert(6, uppercaseLetter);
        letters.insert(12, number);

        return letters.substring(0, 6) + "-" + letters.substring(6, 12) + "-" + letters.substring(12, 18);
    }

    private boolean isValidFilename(String filename) {
        String invalidChars = "/\\:*?\"<>|";
        for (char c : filename.toCharArray()) {
            if (invalidChars.indexOf(c) != -1) {
                LOGGER.warning("Filename contains invalid character: " + c);
                return false;
            }
        }
        return true;
    }
}
