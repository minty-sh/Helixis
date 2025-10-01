package sh.minty.helixis;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import sh.minty.helixis.commands.ColorCommand;
import sh.minty.helixis.commands.CryptCommand;
import sh.minty.helixis.commands.DateCommand;
import sh.minty.helixis.commands.PasswordCommand;

@Command(
    name = "helixis",
    mixinStandardHelpOptions = true,
    version = "0.0.1",
    description = "A collection of command-line tools.",
    subcommands = {
        PasswordCommand.class,
        ColorCommand.class,
        CryptCommand.class,
        DateCommand.class
    }
)
public class App {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}