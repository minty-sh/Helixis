package sh.minty.helixis;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import sh.minty.helixis.commands.ChecksumCommand;
import sh.minty.helixis.commands.ColorCommand;
import sh.minty.helixis.commands.CryptCommand;
import sh.minty.helixis.commands.DateCommand;
import sh.minty.helixis.commands.DnsCommand;
import sh.minty.helixis.commands.FileCompareCommand;
import sh.minty.helixis.commands.FileTouchCommand;
import sh.minty.helixis.commands.PasswordCommand;
import sh.minty.helixis.commands.PortScanCommand;
import sh.minty.helixis.commands.UrlCommand;
import sh.minty.helixis.commands.UuidCommand;

@Command(name = "helixis", mixinStandardHelpOptions = true, version = "0.0.1", description = "A collection of command-line tools.", subcommands = {
        HelpCommand.class, PasswordCommand.class, ColorCommand.class, CryptCommand.class, DateCommand.class,
        UuidCommand.class, UrlCommand.class, DnsCommand.class, PortScanCommand.class, ChecksumCommand.class,
        FileCompareCommand.class, FileTouchCommand.class})
public class App {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
