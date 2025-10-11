package sh.minty.helixis.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Callable;

@Command(
    name = "filetouch",
    mixinStandardHelpOptions = true,
    description = "Changes file timestamps."
)
public class FileTouchCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The path to the file.")
    private String filePathString;

    @Option(names = {"-d", "--date"}, description = "The new date and time (e.g., '2023-10-26 10:30:00'). Defaults to current time.")
    private String dateString;

    @Option(names = {"-f", "--format"}, description = "The format of the date string (e.g., 'yyyy-MM-dd HH:mm:ss'). Defaults to ISO_LOCAL_DATE_TIME.")
    private String format = "yyyy-MM-dd HH:mm:ss";

    @Option(names = "-m", description = "Change the modification time.", defaultValue = "true")
    private boolean modTime;

    @Option(names = "-a", description = "Change the access time.")
    private boolean accessTime;

    @Option(names = "-c", description = "Change the creation time.")
    private boolean createTime;

    @Override
    public Integer call() {
        var filePath = Paths.get(filePathString);

        if (!Files.exists(filePath)) {
            System.err.println("Error: File not found: " + filePathString);
            return 1;
        }

        FileTime fileTime;
        if (dateString == null) {
            fileTime = FileTime.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        } else {
            try {
                var formatter = DateTimeFormatter.ofPattern(format);
                var localDateTime = LocalDateTime.parse(dateString, formatter);
                fileTime = FileTime.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException e) {
                System.err.println("Error: Could not parse date string with the given format. " + e.getMessage());
                return 1;
            }
        }

        try {
            if (modTime) {
                Files.setLastModifiedTime(filePath, fileTime);
                System.out.println("Last modified time of " + filePathString + " updated to " + fileTime);
            }

            var view = Files.getFileAttributeView(filePath, BasicFileAttributeView.class);
            var lastModified = modTime ? fileTime : null;
            var lastAccess = accessTime ? fileTime : null;
            var create = createTime ? fileTime : null;

            view.setTimes(lastModified, lastAccess, create);

            if (accessTime) {
                System.out.println("Last access time of " + filePathString + " updated to " + fileTime);
            }
            if (createTime) {
                System.out.println("Creation time of " + filePathString + " updated to " + fileTime);
            }

            return 0;
        } catch (IOException e) {
            System.err.println("Error updating file time: " + e.getMessage());
            if (createTime) {
                System.err.println("Note: Modifying creation time is not supported by all file systems.");
            }
            return 1;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            return 1;
        }
    }
}
