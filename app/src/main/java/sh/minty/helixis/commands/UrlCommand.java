package sh.minty.helixis.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

@Command(
    name = "url",
    mixinStandardHelpOptions = true,
    description = "URL encoding and decoding utilities.",
    subcommands = {
        UrlCommand.EncodeCommand.class,
        UrlCommand.DecodeCommand.class
    }
)
public class UrlCommand {
    @Command(name = "encode", mixinStandardHelpOptions = true, description = "URL-encodes a string.")
    static class EncodeCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The string to URL-encode.")
        private String inputString;

        @Option(names = {"-c", "--charset"}, description = "The charset to use for encoding (e.g., UTF-8). Defaults to UTF-8.")
        private String charset = StandardCharsets.UTF_8.name();

        @Override
        public Integer call() throws Exception {
            try {
                String encodedString = URLEncoder.encode(inputString, charset);
                System.out.println(encodedString);
                return 0;
            } catch (Exception e) {
                System.err.println("Error encoding URL: " + e.getMessage());
                return 1;
            }
        }
    }

    @Command(name = "decode", mixinStandardHelpOptions = true, description = "URL-decodes a string.")
    static class DecodeCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The string to URL-decode.")
        private String inputString;

        @Option(names = {"-c", "--charset"}, description = "The charset to use for decoding (e.g., UTF-8). Defaults to UTF-8.")
        private String charset = StandardCharsets.UTF_8.name();

        @Override
        public Integer call() throws Exception {
            try {
                String decodedString = URLDecoder.decode(inputString, charset);
                System.out.println(decodedString);
                return 0;
            } catch (Exception e) {
                System.err.println("Error decoding URL: " + e.getMessage());
                return 1;
            }
        }
    }
}
