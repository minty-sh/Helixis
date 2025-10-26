package sh.minty.helixis.commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

record GenerationParams(int length, int numUppercase, int numLowercase, int numDigits, int numSpecial,
        String specialChars, char separator, int separatorFrequency) {
}

@Command(name = "password", mixinStandardHelpOptions = true, description = "Generate X number of random passwords.")
public class PasswordCommand implements Callable<Integer> {
    private static final Logger LOGGER = Logger.getLogger(PasswordCommand.class.getName());
    private SecureRandom random = new SecureRandom();

    @Option(names = {"-n", "--number"}, description = "generate x passwords (default: 1)")
    private int numPasswords = 1;

    @Option(names = {"-f", "--file"}, description = "write passwords to filename")
    private String filename;

    @Option(names = {"-l",
            "--length"}, description = "Total length of the password (default: 18). This length excludes separators.")
    private int length = 18;

    @Option(names = {"-u",
            "--uppercase"}, description = "Number of uppercase letters (default: 1). If not specified, a random number of uppercase letters will be used to meet the total length requirement.")
    private int numUppercase = 1;

    @Option(names = {"-c",
            "--lowercase"}, description = "Number of lowercase letters (default: calculated). If not specified, a random number of lowercase letters will be used to meet the total length requirement.")
    private int numLowercase = -1; // -1 indicates calculated based on total length and other character types

    @Option(names = {"-d",
            "--digits"}, description = "Number of digits (default: 1). If not specified, a random number of digits will be used to meet the total length requirement.")
    private int numDigits = 1;

    @Option(names = {"-s",
            "--special"}, description = "Number of special characters (default: 0). If not specified, a random number of special characters will be used to meet the total length requirement.")
    private int numSpecial = 0;

    @Option(names = {
            "--special-chars"}, description = "Allowed special characters (default: !@#$%^&*()-_=+[{]}\\|;:'\",<.>/?).")
    private String specialChars = "!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?";

    @Option(names = {"--separator"}, description = "Separator character (default: -).")
    private char separator = '-';

    @Option(names = {
            "--separator-frequency"}, description = "Frequency of separator (e.g., 6 for XXXXXX-XXXXXX-XXXXXX) (default: 6). Set to 0 for no separators.")
    private int separatorFrequency = 6;

    @Override
    public Integer call() {
        if (numPasswords <= 0) {
            System.err.println("Error: Invalid number of passwords.");
            return 1;
        }

        // calculate numLowercase if not explicitly set
        if (numLowercase == -1) {
            numLowercase = length - numUppercase - numDigits - numSpecial;
            if (numLowercase < 0) {
                System.err.println(
                        "Error: Total length is less than the sum of required uppercase, digits, and special characters.");
                return 1;
            }
        }

        if (numUppercase < 0 || numDigits < 0 || numSpecial < 0 || numLowercase < 0) {
            System.err.println("Error: Number of character types cannot be negative.");
            return 1;
        }

        if (numUppercase + numDigits + numSpecial + numLowercase != length) {
            System.err.println("Error: Sum of character types does not match the total length.");
            return 1;
        }

        var params = new GenerationParams(length, numUppercase, numLowercase, numDigits, numSpecial, specialChars,
                separator, separatorFrequency);

        List<String> passwords = new ArrayList<>();
        for (int i = 0; i < numPasswords; i++) {
            passwords.add(generatePassword(params));
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

    private String generatePassword(GenerationParams params) {
        final String lowercaseChars = "abcdefghijklmnopqrstuvwxyz";
        final String uppercaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String digitChars = "0123456789";

        List<Character> passwordChars = new ArrayList<>();

        // add required uppercase letters
        for (int i = 0; i < params.numUppercase(); i++) {
            passwordChars.add(uppercaseChars.charAt(random.nextInt(uppercaseChars.length())));
        }

        // add required digits
        for (int i = 0; i < params.numDigits(); i++) {
            passwordChars.add(digitChars.charAt(random.nextInt(digitChars.length())));
        }

        // add required special characters
        for (int i = 0; i < params.numSpecial(); i++) {
            passwordChars.add(params.specialChars().charAt(random.nextInt(params.specialChars().length())));
        }

        // fill the rest with lowercase letters
        for (int i = 0; i < params.numLowercase(); i++) {
            passwordChars.add(lowercaseChars.charAt(random.nextInt(lowercaseChars.length())));
        }

        Collections.shuffle(passwordChars, random);

        // build the password string
        var passwordBuilder = new StringBuilder();
        for (int i = 0; i < passwordChars.size(); i++) {
            passwordBuilder.append(passwordChars.get(i));
            if (params.separatorFrequency() > 0 && (i + 1) % params.separatorFrequency() == 0
                    && (i + 1) < params.length()) {
                passwordBuilder.append(params.separator());
            }
        }

        return passwordBuilder.toString();
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
