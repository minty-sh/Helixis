package sh.minty.helixis.commands;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import sh.minty.helixis.App;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ColorCommandTest {

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
    public void testColorCommand() {
        new CommandLine(new App()).execute("color", "255", "0", "0");
        String output = outContent.toString();
        assertTrue(output.contains("RGB values (255, 0, 0) are equivalent to hex value: #ff0000"));
        assertTrue(output.contains("Here are 5 similar colors:"));
        long count = output.lines().filter(line -> line.startsWith("#")).count();
        assertEquals(5, count); // 5 similar colors
    }
}
