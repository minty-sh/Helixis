package sh.minty.helixis.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.concurrent.Callable;

@Command(name = "hash", mixinStandardHelpOptions = true, description = "Converts text to a number using FNV-1a hash.")
public class HashCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The file to read from. If not provided, reads from stdin.", arity = "0..1")
    private File inputFile;

    @Override
    public Integer call() throws Exception {
        String input;
        if (inputFile != null) {
            if (!inputFile.exists()) {
                System.err.println("Error: Unable to open file.");
                return 1;
            }
            input = new String(Files.readAllBytes(inputFile.toPath()));
        } else {
            var scanner = new Scanner(System.in);
            var sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine()).append(System.lineSeparator());
            }
            scanner.close();
            input = sb.toString().trim();
        }

        if (input.isEmpty()) {
            System.err.println("Error: Please provide an argument.");
            return 1;
        }

        String hash = convertTextToNumber(input);
        System.out.println(hash);
        return 0;
    }

    private String convertTextToNumber(String input) {
        final long FNV1_64_INIT = 0xcbf29ce484222325L;
        final long FNV1_PRIME_64 = 0x100000001b3L;

        long hash = FNV1_64_INIT;
        for (int i = 0; i < input.length(); i++) {
            hash ^= input.charAt(i);
            hash *= FNV1_PRIME_64;
        }

        long result = Math.abs(hash % 9223372036854775807L);
        String resultStr = Long.toString(result);
        if (resultStr.length() >= 16) {
            return resultStr;
        }
        var sb = new StringBuilder(resultStr);
        while (sb.length() < 16) {
            sb.append('0');
        }
        return sb.toString();
    }
}
