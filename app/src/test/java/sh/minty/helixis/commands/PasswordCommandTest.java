package sh.minty.helixis.commands;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import sh.minty.helixis.App;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordCommandTest {

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testPasswordDefault() {
        new CommandLine(new App()).execute("password");
        String output = outContent.toString().trim();
        assertTrue(output.matches("[a-z]{6}-[A-Z][a-z]{5}-[0-9][a-z]{5}"));
    }

    @Test
    public void testPasswordToFile(@TempDir Path tempDir) throws IOException {
        File tempFile = tempDir.resolve("passwords.txt").toFile();
        int exitCode = new CommandLine(new App()).execute("password", "-n", "5", "-f", tempFile.getAbsolutePath());
        assertEquals(0, exitCode, "Command should exit with 0, but exited with " + exitCode + ". Stderr: " + errContent.toString());
        
        List<String> lines = Files.readAllLines(tempFile.toPath());
        assertEquals(5, lines.size());
        for (String line : lines) {
            assertTrue(line.matches("[a-z]{6}-[A-Z][a-z]{5}-[0-9][a-z]{5}"));
        }
        assertTrue(outContent.toString().contains("File written!"));
    }
}