package sh.minty.helixis.commands;

import picocli.CommandLine.Command;

@Command(name = "date", mixinStandardHelpOptions = true, description = "Date utilities.", subcommands = { DateDifference.class })
public class DateCommand {}
