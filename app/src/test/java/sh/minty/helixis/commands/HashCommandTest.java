package sh.minty.helixis.commands;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import sh.minty.helixis.App;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HashCommandTest {

    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    public void testHashWithStdin() {
        String input = "hello world";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        new CommandLine(new App()).execute("hash");
        String output = outContent.toString().trim();
        assertTrue(output.matches("\\d{16,}"));
    }

    @Test
    public void testHashWithFile(@TempDir Path tempDir) throws IOException {
        File tempFile = tempDir.resolve("test.txt").toFile();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("hello world");
        }
        new CommandLine(new App()).execute("hash", tempFile.getAbsolutePath());
        String output = outContent.toString().trim();
        assertTrue(output.matches("\\d{16,}"));
    }
}