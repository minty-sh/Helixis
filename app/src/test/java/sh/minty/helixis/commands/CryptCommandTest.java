package sh.minty.helixis.commands;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import sh.minty.helixis.App;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CryptCommandTest {

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
    public void testEncrypt(@TempDir Path tempDir) throws IOException {
        File inputFile = tempDir.resolve("input.txt").toFile();
        String originalText = "Hello World 123!";
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write(originalText);
        }

        new CommandLine(new App()).execute("crypt", "encrypt", inputFile.getAbsolutePath());
        String encryptedText = "Svool Dliow 123!";
        assertTrue(outContent.toString().contains(encryptedText));
    }

    @Test
    public void testDecrypt(@TempDir Path tempDir) throws IOException {
        File inputFile = tempDir.resolve("input.txt").toFile();
        String encryptedText = "Svool Dliow 123!";
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write(encryptedText);
        }

        new CommandLine(new App()).execute("crypt", "decrypt", inputFile.getAbsolutePath());
        String originalText = "Hello World 123!";
        assertTrue(outContent.toString().contains(originalText));
    }
}
