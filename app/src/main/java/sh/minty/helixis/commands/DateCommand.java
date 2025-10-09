package sh.minty.helixis.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.Year;
import java.time.ZoneId;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

@Command(
    name = "date",
    mixinStandardHelpOptions = true,
    description = "Date utilities.",
    subcommands = {
        DateDifference.class, DateCommand.CurrentDateTimeCommand.class,
        DateCommand.FormatCommand.class, DateCommand.ArithmeticCommand.class,
        DateCommand.BusinessDayArithmeticCommand.class, DateCommand.TimestampCommand.class,
        DateCommand.ExtractCommand.class, DateCommand.LeapYearCommand.class,
        DateCommand.TimezoneConvertCommand.class, DateCommand.CalendarCommand.class,
        DateCommand.RandomCommand.class
    }
)
public class DateCommand {
    @Command(name = "now", mixinStandardHelpOptions = true, description = "Display the current date and time with GNU date formatting options.")
    static class CurrentDateTimeCommand implements Callable<Integer> {
        @Option(names = {"-f", "--format"}, description = "Specify output format (e.g., 'yyyy-MM-dd HH:mm:ss'). Defaults to ISO_LOCAL_DATE_TIME.")
        private String format = "yyyy-MM-dd HH:mm:ss";

        @Option(names = {"-z", "--zone"}, description = "Specify time zone (e.g., 'America/New_York'). Defaults to system default.")
        private String zoneId;

        @Option(names = {"--gnu-format"}, description = "Treat output format (-f) as GNU date format.")
        private boolean gnuFormat;

        @Override
        public Integer call() {
            var now = LocalDateTime.now();
            var targetZone = (zoneId != null) ? ZoneId.of(zoneId) : ZoneId.systemDefault();

            String finalFormat = format;
            if (gnuFormat) {
                finalFormat = convertGnuFormatToJava(format);
            }

            // Special handling for epoch seconds (%s)
            if (finalFormat.contains("epoch_seconds")) {
                System.out.println(now.atZone(ZoneId.systemDefault()).toEpochSecond());
            } else {
                var formatter = DateTimeFormatter.ofPattern(finalFormat).withZone(targetZone);
                System.out.println(now.atZone(ZoneId.systemDefault()).withZoneSameInstant(targetZone).format(formatter));
            }
            return 0;
        }
    }

    @Command(name = "format", mixinStandardHelpOptions = true, description = "Format a given date string into a specified output with GNU date formatting options.")
    static class FormatCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The date string to format.")
        private String dateString;

        @Option(names = {"-i", "--input-format"}, description = "Specify input format (e.g., 'yyyy-MM-dd HH:mm:ss'). Defaults to ISO_LOCAL_DATE_TIME.")
        private String inputFormat = "yyyy-MM-dd HH:mm:ss";

        @Option(names = {"-o", "--output-format"}, description = "Specify output format (e.g., 'yyyy/MM/dd HH:mm:ss'). Defaults to ISO_LOCAL_DATE_TIME.")
        private String outputFormat = "yyyy-MM-dd HH:mm:ss";

        @Option(names = {"-z", "--zone"}, description = "Specify time zone for parsing and formatting (e.g., 'America/New_York'). Defaults to system default.")
        private String zoneId;

        @Option(names = {"--gnu-input-format"}, description = "Treat input format (-i) as GNU date format.")
        private boolean gnuInputFormat;

        @Option(names = {"--gnu-output-format"}, description = "Treat output format (-o) as GNU date format.")
        private boolean gnuOutputFormat;

        @Override
        public Integer call() {
            try {
                var targetZone = (zoneId != null) ? ZoneId.of(zoneId) : ZoneId.systemDefault();

                String finalInputFormat = inputFormat;
                if (gnuInputFormat) {
                    finalInputFormat = convertGnuFormatToJava(inputFormat);
                }

                String finalOutputFormat = outputFormat;
                if (gnuOutputFormat) {
                    finalOutputFormat = convertGnuFormatToJava(outputFormat);
                }

                var inputFormatter = DateTimeFormatter.ofPattern(finalInputFormat).withZone(targetZone);
                var outputFormatter = DateTimeFormatter.ofPattern(finalOutputFormat).withZone(targetZone);

                var dateTime = LocalDateTime.parse(dateString, inputFormatter);

                // Special handling for epoch seconds (%s) in output
                if (finalOutputFormat.contains("epoch_seconds")) {
                    System.out.println(dateTime.atZone(targetZone).toEpochSecond());
                } else {
                    System.out.println(dateTime.atZone(targetZone).format(outputFormatter));
                }
            } catch (DateTimeParseException e) {
                System.err.println("Error: Could not parse date string with the given input format. " + e.getMessage());
                return 1;
            }
            return 0;
        }
    }

    @Command(name = "arithmetic", mixinStandardHelpOptions = true, description = "Add or subtract durations from a given date.")
    static class ArithmeticCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The date string to perform arithmetic on.")
        private String dateString;

        @Parameters(index = "1", description = "The amount to add or subtract.")
        private long amount;

        @Parameters(index = "2", description = "The unit of the amount (e.g., YEARS, MONTHS, DAYS, HOURS, MINUTES, SECONDS).")
        private ChronoUnit unit;

        @Option(names = {"-i", "--input-format"}, description = "Specify input format (e.g., 'yyyy-MM-dd HH:mm:ss'). Defaults to ISO_LOCAL_DATE_TIME.")
        private String inputFormat = "yyyy-MM-dd HH:mm:ss";

        @Option(names = {"-o", "--output-format"}, description = "Specify output format (e.g., 'yyyy/MM/dd HH:mm:ss'). Defaults to ISO_LOCAL_DATE_TIME.")
        private String outputFormat = "yyyy-MM-dd HH:mm:ss";

        @Option(names = {"-z", "--zone"}, description = "Specify time zone for parsing and formatting (e.g., 'America/New_York'). Defaults to system default.")
        private String zoneId;

        @Option(names = {"--gnu-input-format"}, description = "Treat input format (-i) as GNU date format.")
        private boolean gnuInputFormat;

        @Option(names = {"--gnu-output-format"}, description = "Treat output format (-o) as GNU date format.")
        private boolean gnuOutputFormat;

        @Override
        public Integer call() {
            try {
                var targetZone = (zoneId != null) ? ZoneId.of(zoneId) : ZoneId.systemDefault();

                String finalInputFormat = inputFormat;
                if (gnuInputFormat) {
                    finalInputFormat = convertGnuFormatToJava(inputFormat);
                }

                String finalOutputFormat = outputFormat;
                if (gnuOutputFormat) {
                    finalOutputFormat = convertGnuFormatToJava(outputFormat);
                }

                var inputFormatter = DateTimeFormatter.ofPattern(finalInputFormat).withZone(targetZone);
                var dateTime = LocalDateTime.parse(dateString, inputFormatter);

                LocalDateTime resultDateTime;
                if (amount >= 0) {
                    resultDateTime = dateTime.plus(amount, unit);
                } else {
                    resultDateTime = dateTime.minus(Math.abs(amount), unit);
                }

                // Special handling for epoch seconds (%s) in output
                if (finalOutputFormat.contains("epoch_seconds")) {
                    System.out.println(resultDateTime.atZone(targetZone).toEpochSecond());
                } else {
                    var outputFormatter = DateTimeFormatter.ofPattern(finalOutputFormat).withZone(targetZone);
                    System.out.println(resultDateTime.atZone(targetZone).format(outputFormatter));
                }
            } catch (DateTimeParseException e) {
                System.err.println("Error: Could not parse date string with the given input format. " + e.getMessage());
                return 1;
            } catch (IllegalArgumentException e) {
                System.err.println("Error: Invalid unit or amount. " + e.getMessage());
                return 1;
            }
            return 0;
        }
    }

    @Command(
        name = "business-arithmetic",
        mixinStandardHelpOptions = true,
        description = "Calculate the date after or before a specified number of business days."
    )
    static class BusinessDayArithmeticCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The date string to perform arithmetic on.")
        private String dateString;

        @Parameters(index = "1", description = "The number of business days to add or subtract.")
        private int businessDays;

        @Option(names = {"-i", "--input-format"}, description = "Specify input format (e.g., 'yyyy-MM-dd'). Defaults to ISO_LOCAL_DATE.")
        private String inputFormat = "yyyy-MM-dd";

        @Option(names = {"-o", "--output-format"}, description = "Specify output format (e.g., 'yyyy/MM/dd'). Defaults to ISO_LOCAL_DATE.")
        private String outputFormat = "yyyy-MM-dd";

        @Override
        public Integer call() {
            try {
                var inputFormatter = DateTimeFormatter.ofPattern(inputFormat);
                var dateTime = LocalDateTime.parse(dateString, inputFormatter);

                var resultDateTime = dateTime;
                int daysCount = 0;
                while (daysCount < Math.abs(businessDays)) {
                    resultDateTime = resultDateTime.plusDays((businessDays > 0) ? 1 : -1);
                    if (!(resultDateTime.getDayOfWeek() == DayOfWeek.SATURDAY || resultDateTime.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                        daysCount++;
                    }
                }

                var outputFormatter = DateTimeFormatter.ofPattern(outputFormat);
                System.out.println(resultDateTime.format(outputFormatter));
            } catch (DateTimeParseException e) {
                System.err.println("Error: Could not parse date string with the given input format. " + e.getMessage());
                return 1;
            }
            return 0;
        }
    }

    @Command(
        name = "timestamp",
        mixinStandardHelpOptions = true,
        description = "Convert between human-readable dates and Unix timestamps, and vice-versa.",
        subcommands = { DateCommand.ToTimestampCommand.class, DateCommand.FromTimestampCommand.class }
    )
    static class TimestampCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            // This command acts as a container for sub-subcommands
            return 0;
        }
    }

    @Command(
        name = "to-timestamp",
        mixinStandardHelpOptions = true,
        description = "Convert a human-readable date to a Unix timestamp."
    )
    static class ToTimestampCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The date string to convert to timestamp.")
        private String dateString;

        @Option(
            names = {"-i", "--input-format"},
            description = "Specify input format (e.g., 'yyyy-MM-dd HH:mm:ss'). Defaults to ISO_LOCAL_DATE_TIME."
        )
        private String inputFormat = "yyyy-MM-dd HH:mm:ss";

        @Option(
            names = {"-z", "--zone"},
            description = "Specify time zone for parsing (e.g., 'America/New_York'). Defaults to system default."
        )
        private String zoneId;

        @Option(names = {"--gnu-input-format"}, description = "Treat input format (-i) as GNU date format.")
        private boolean gnuInputFormat;

        @Override
        public Integer call() {
            try {
                var targetZone = (zoneId != null) ? ZoneId.of(zoneId) : ZoneId.systemDefault();

                String finalInputFormat = inputFormat;
                if (gnuInputFormat) {
                    finalInputFormat = convertGnuFormatToJava(inputFormat);
                }

                var formatter = DateTimeFormatter.ofPattern(finalInputFormat).withZone(targetZone);
                var dateTime = LocalDateTime.parse(dateString, formatter);
                var timestamp = dateTime.atZone(targetZone).toEpochSecond();
                System.out.println(timestamp);
            } catch (DateTimeParseException e) {
                System.err.println("Error: Could not parse date string with the given input format. " + e.getMessage());
                return 1;
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                return 1;
            }
            return 0;
        }
    }

    @Command(
        name = "from-timestamp",
        mixinStandardHelpOptions = true,
        description = "Convert a Unix timestamp to a human-readable date."
    )
    static class FromTimestampCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The Unix timestamp to convert.")
        private long timestamp;

        @Option(names = {"-f", "--format"}, description = "Specify output format (e.g., 'yyyy-MM-dd HH:mm:ss'). Defaults to ISO_LOCAL_DATE_TIME.")
        private String outputFormat = "yyyy-MM-dd HH:mm:ss";

        @Option(names = {"-z", "--zone"}, description = "Specify time zone for formatting (e.g., 'America/New_York'). Defaults to system default.")
        private String zoneId;

        @Option(names = {"--gnu-format"}, description = "Treat output format (-f) as GNU date format.")
        private boolean gnuFormat;

        @Override
        public Integer call() {
            try {
                var targetZone = (zoneId != null) ? ZoneId.of(zoneId) : ZoneId.systemDefault();

                String finalOutputFormat = outputFormat;
                if (gnuFormat) {
                    finalOutputFormat = convertGnuFormatToJava(outputFormat);
                }

                // Special handling for epoch seconds (%s)
                if (finalOutputFormat.contains("epoch_seconds")) {
                    System.out.println(timestamp);
                } else {
                    var formatter = DateTimeFormatter.ofPattern(finalOutputFormat).withZone(targetZone);
                    var dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), targetZone);
                    System.out.println(dateTime.atZone(targetZone).format(formatter));
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                return 1;
            }
            return 0;
        }
    }

    @Command(
        name = "extract",
        mixinStandardHelpOptions = true,
        description = "Extract components from a given date."
    )
    static class ExtractCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The date string to extract components from.")
        private String dateString;

        @Parameters(
            index = "1",
            description = "The component to extract (e.g., DAY_OF_WEEK, DAY_OF_MONTH, DAY_OF_YEAR, YEAR, MONTH, HOUR, MINUTE, SECOND)."
        )
        private ChronoField component;

        @Option(
            names = {"-i", "--input-format"},
            description = "Specify input format (e.g., 'yyyy-MM-dd HH:mm:ss'). Defaults to ISO_LOCAL_DATE_TIME."
        )
        private String inputFormat = "yyyy-MM-dd HH:mm:ss";

        @Override
        public Integer call() {
            try {
                var inputFormatter = DateTimeFormatter.ofPattern(inputFormat);
                var dateTime = LocalDateTime.parse(dateString, inputFormatter);

                switch (component) {
                    case DAY_OF_WEEK -> System.out.println(dateTime.getDayOfWeek().name());
                    case DAY_OF_MONTH -> System.out.println(dateTime.getDayOfMonth());
                    case DAY_OF_YEAR -> System.out.println(dateTime.getDayOfYear());
                    case YEAR -> System.out.println(dateTime.getYear());
                    case MONTH_OF_YEAR -> System.out.println(dateTime.getMonth().name());
                    case HOUR_OF_DAY -> System.out.println(dateTime.getHour());
                    case MINUTE_OF_HOUR -> System.out.println(dateTime.getMinute());
                    case SECOND_OF_MINUTE -> System.out.println(dateTime.getSecond());
                    default -> {
                        System.err.println("Error: Unsupported date component for extraction: " + component);
                        return 1;
                    }
                }
            } catch (DateTimeParseException e) {
                System.err.println("Error: Could not parse date string with the given input format. " + e.getMessage());
                return 1;
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                return 1;
            }
            return 0;
        }
    }

    @Command(
        name = "leap-year",
        mixinStandardHelpOptions = true,
        description = "Determine if a given year is a leap year."
    )
    static class LeapYearCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The year to check.")
        private int year;

        @Override
        public Integer call() {
            System.out.println(Year.of(year).isLeap());
            return 0;
        }
    }

    @Command(
        name = "timezone-convert",
        mixinStandardHelpOptions = true,
        description = "Convert a date and time from one time zone to another."
    )
    static class TimezoneConvertCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The date string to convert.")
        private String dateString;

        @Parameters(index = "1", description = "The source time zone ID (e.g., 'America/New_York').")
        private String sourceZoneId;

        @Parameters(index = "2", description = "The target time zone ID (e.g., 'Europe/London').")
        private String targetZoneId;

        @Option(
            names = {"-i", "--input-format"},
            description = "Specify input format (e.g., 'yyyy-MM-dd HH:mm:ss'). Defaults to ISO_LOCAL_DATE_TIME."
        )
        private String inputFormat = "yyyy-MM-dd HH:mm:ss";

        @Option(
            names = {"-o", "--output-format"},
            description = "Specify output format (e.g., 'yyyy-MM-dd HH:mm:ss'). Defaults to ISO_LOCAL_DATE_TIME."
        )
        private String outputFormat = "yyyy-MM-dd HH:mm:ss";

        @Option(names = {"--gnu-input-format"}, description = "Treat input format (-i) as GNU date format.")
        private boolean gnuInputFormat;

        @Option(names = {"--gnu-output-format"}, description = "Treat output format (-o) as GNU date format.")
        private boolean gnuOutputFormat;

        @Override
        public Integer call() {
            try {
                String finalInputFormat = inputFormat;
                if (gnuInputFormat) {
                    finalInputFormat = convertGnuFormatToJava(inputFormat);
                }

                String finalOutputFormat = outputFormat;
                if (gnuOutputFormat) {
                    finalOutputFormat = convertGnuFormatToJava(outputFormat);
                }

                var inputFormatter = DateTimeFormatter.ofPattern(finalInputFormat);
                var localDateTime = LocalDateTime.parse(dateString, inputFormatter);
                var sourceZonedDateTime = localDateTime.atZone(ZoneId.of(sourceZoneId));
                var targetZonedDateTime = sourceZonedDateTime.withZoneSameInstant(ZoneId.of(targetZoneId));

                // Special handling for epoch seconds (%s) in output
                if (finalOutputFormat.contains("epoch_seconds")) {
                    System.out.println(targetZonedDateTime.toEpochSecond());
                } else {
                    var outputFormatter = DateTimeFormatter.ofPattern(finalOutputFormat).withZone(ZoneId.of(targetZoneId));
                    System.out.println(targetZonedDateTime.format(outputFormatter));
                }

            } catch (DateTimeParseException e) {
                System.err.println("Error: Could not parse date string with the given input format or zone. " + e.getMessage());
                return 1;
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                return 1;
            }
            return 0;
        }
    }

    @Command(name = "calendar", mixinStandardHelpOptions = true, description = "Show a simple calendar for a specific month and year.")
    static class CalendarCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "The year for the calendar.")
        private int year;

        @Parameters(index = "1", description = "The month for the calendar (1-12).")
        private int month;

        @Override
        public Integer call() {
            try {
                var firstDayOfMonth = LocalDate.of(year, month, 1);
                int daysInMonth = firstDayOfMonth.lengthOfMonth();
                var dayOfWeek = firstDayOfMonth.getDayOfWeek();

                System.out.println("   " + firstDayOfMonth.getMonth().name() + " " + year);
                System.out.println("Mo Tu We Th Fr Sa Su");

                // print leading spaces
                for (int i = 1; i < dayOfWeek.getValue(); i++) {
                    System.out.print("   ");
                }

                for (int day = 1; day <= daysInMonth; day++) {
                    System.out.printf("%2d ", day);
                    if (LocalDate.of(year, month, day).getDayOfWeek() == DayOfWeek.SUNDAY) {
                        System.out.println();
                    }
                }

                // new line at the end
                System.out.println();

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                return 1;
            }
            return 0;
        }
    }

    private static String convertGnuFormatToJava(String gnuFormat) {
        return gnuFormat
            .replace("%a", "E")
            .replace("%A", "EEEE")
            .replace("%b", "MMM")
            .replace("%B", "MMMM")
            .replace("%c", "E MMM d HH:mm:ss yyyy")
            .replace("%C", "yy") // Century, last two digits
            .replace("%d", "dd")
            .replace("%D", "MM/dd/yy")
            .replace("%e", "d")
            .replace("%F", "yyyy-MM-dd")
            .replace("%g", "yy")
            .replace("%G", "YYYY")
            .replace("%h", "MMM")
            .replace("%H", "HH")
            .replace("%I", "hh")
            .replace("%j", "D")
            .replace("%k", "H")
            .replace("%l", "h")
            .replace("%m", "MM")
            .replace("%M", "mm")
            .replace("%n", "\n")
            .replace("%N", "n")
            .replace("%p", "a")
            .replace("%P", "a") // lower case am/pm
            .replace("%q", "Q")
            .replace("%r", "hh:mm:ss a")
            .replace("%R", "HH:mm")
            .replace("%s", "epoch_seconds") // Special handling
            .replace("%S", "ss")
            .replace("%t", "\t")
            .replace("%T", "HH:mm:ss")
            .replace("%u", "e")
            .replace("%U", "ww")
            .replace("%V", "WW")
            .replace("%w", "e") // Day of week (0-6), 0 is Sunday
            .replace("%W", "ww")
            .replace("%x", "MM/dd/yy")
            .replace("%X", "HH:mm:ss")
            .replace("%y", "yy")
            .replace("%Y", "yyyy")
            .replace("%z", "Z")
            .replace("%:z", "XXX")
            .replace("%::z", "XXX")
            .replace("%:::z", "XXX")
            .replace("%Z", "z");
    }

    @Command(name = "random", mixinStandardHelpOptions = true, description = "Generate a random date with parameters.")
    static class RandomCommand implements Callable<Integer> {
        @Option(names = {"--after"}, description = "Generate a date after this date (e.g., '2023-01-01').")
        private String afterDateString;

        @Option(names = {"--before"}, description = "Generate a date before this date (e.g., '2024-12-31').")
        private String beforeDateString;

        @Option(names = {"-f", "--format"}, description = "Output format for the generated date (e.g., 'yyyy-MM-dd'). Defaults to ISO_LOCAL_DATE.")
        private String outputFormat = "yyyy-MM-dd";

        @Option(names = {"--gnu-output-format"}, description = "Treat output format (-f) as GNU date format.")
        private boolean gnuOutputFormat;

        @Override
        public Integer call() {
            try {
                var minDate = LocalDate.of(1900, 1, 1); // default min date
                var maxDate = LocalDate.of(2100, 12, 31); // default max date

                if (afterDateString != null) {
                    minDate = LocalDate.parse(afterDateString, DateTimeFormatter.ISO_LOCAL_DATE);
                }
                if (beforeDateString != null) {
                    maxDate = LocalDate.parse(beforeDateString, DateTimeFormatter.ISO_LOCAL_DATE);
                }

                if (minDate.isAfter(maxDate)) {
                    System.err.println("Error: --after date cannot be after --before date.");
                    return 1;
                }

                long minEpochDay = minDate.toEpochDay();
                long maxEpochDay = maxDate.toEpochDay();

                long randomEpochDay = ThreadLocalRandom.current().nextLong(minEpochDay, maxEpochDay + 1);
                var randomDate = LocalDate.ofEpochDay(randomEpochDay);

                String finalOutputFormat = outputFormat;
                if (gnuOutputFormat) {
                    finalOutputFormat = convertGnuFormatToJava(outputFormat);
                }

                // Special handling for epoch seconds (%s)
                if (finalOutputFormat.contains("epoch_seconds")) {
                    System.out.println(randomDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
                } else {
                    var formatter = DateTimeFormatter.ofPattern(finalOutputFormat);
                    System.out.println(randomDate.format(formatter));
                }

            } catch (DateTimeParseException e) {
                System.err.println("Error: Could not parse date string. Please use 'yyyy-MM-dd' format for --after and --before. " + e.getMessage());
                return 1;
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                return 1;
            }
            return 0;
        }
    }
}