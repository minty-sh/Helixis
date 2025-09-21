package sh.minty.helixis.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Callable;

@Command(name = "difference", mixinStandardHelpOptions = true, description = "Calculates the difference between two dates.")
public class DateDifference implements Callable<Integer> {
    // TODO: add `date`-like pattern customization
    @Parameters(index = "0", description = "The first (older) date in 'yyyy-MM-dd HH:mm:ss' format.")
    private String firstDateStr;

    @Parameters(index = "1", description = "The second (newer) date in 'yyyy-MM-dd HH:mm:ss' format.")
    private String lastDateStr;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Integer call() {
        try {
            LocalDateTime firstDate = LocalDateTime.parse(firstDateStr, FORMATTER);
            LocalDateTime lastDate = LocalDateTime.parse(lastDateStr, FORMATTER);

            if (firstDate.isAfter(lastDate)) {
                System.err.println("Error: The first date must be before the second date.");
                return 1;
            }

            Duration duration = Duration.between(firstDate, lastDate);

            long days = duration.toDays();
            long years = days / 365;
            days %= 365;
            long months = days / 30; // Approximation
            days %= 30;
            long weeks = days / 7;
            days %= 7;

            long hours = duration.toHours() % 24;
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;

            System.out.printf("Time from %s to %s:%n", firstDate.format(FORMATTER), lastDate.format(FORMATTER));
            
            StringBuilder result = new StringBuilder();
            appendUnit(result, years, "year");
            appendUnit(result, months, "month");
            appendUnit(result, weeks, "week");
            appendUnit(result, days, "day");
            appendUnit(result, hours, "hour");
            appendUnit(result, minutes, "minute");
            appendUnit(result, seconds, "second");

            System.out.println(result.toString());

        } catch (DateTimeParseException e) {
            System.err.println("Error: Invalid date format. Please use 'yyyy-MM-dd HH:mm:ss'.");
            return 1;
        }
        return 0;
    }

    private void appendUnit(StringBuilder sb, long value, String unit) {
        if (value > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(value).append(" ").append(unit);
            if (value > 1) {
                sb.append("s");
            }
        }
    }
}
