package sh.minty.helixis.commands;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import sh.minty.helixis.App;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateCommandTest {

    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    public void testDateCommand() {
        String firstDate = "2023-01-01 12:00:00";
        String lastDate = "2024-02-03 14:30:30";
        new CommandLine(new App()).execute("date", firstDate, lastDate);
        String output = outContent.toString();
        
        assertTrue(output.contains("Time from 2023-01-01 12:00:00 to 2024-02-03 14:30:30:"));
        assertTrue(output.contains("1 year, 1 month, 3 days, 2 hours, 30 minutes, 30 seconds"));
    }
}
