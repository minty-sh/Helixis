package sh.minty.helixis.commands;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "filecompare", mixinStandardHelpOptions = true, description = "Compares two files byte by byte.")
public class FileCompareCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The path to the first file.")
    private String filePath1;

    @Parameters(index = "1", description = "The path to the second file.")
    private String filePath2;

    @Override
    public Integer call() {
        Path path1 = Paths.get(filePath1);
        Path path2 = Paths.get(filePath2);

        if (!Files.exists(path1)) {
            System.err.println("Error: File not found: " + filePath1);
            return 1;
        }
        if (!Files.exists(path2)) {
            System.err.println("Error: File not found: " + filePath2);
            return 1;
        }

        try {
            long mismatch = Files.mismatch(path1, path2);
            if (mismatch == -1) {
                System.out.println("Files are identical.");
                return 0;
            } else {
                System.out.println("Files differ at byte position: " + mismatch);
                return 1;
            }
        } catch (Exception e) {
            System.err.println("Error comparing files: " + e.getMessage());
            return 1;
        }
    }
}
