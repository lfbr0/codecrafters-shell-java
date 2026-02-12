package command;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathCommand implements CodeCraftersShellCommand {
    private final Path cmdPath;
    private final File currentWorkDirectoryFile;

    public PathCommand(Path cmdPath, File currentWorkDirectoryFile) {
        this.cmdPath = cmdPath;
        this.currentWorkDirectoryFile = currentWorkDirectoryFile;
    }

    @Override
    public void execute(OutputStream outputStream, OutputStream errorStream, String... args) throws Exception {
        // form args
        List<String> argsList = new ArrayList<>(args.length + 1);
        argsList.add(cmdPath.getFileName().toString());
        argsList.addAll(Arrays.asList(args));

        // create process at current working directory
        ProcessBuilder processBuilder = new ProcessBuilder(argsList).directory(currentWorkDirectoryFile);
        Process process = processBuilder.start();

        // transfer concurrently - no transferTo so is not blocking...
        Thread stdOutThread = new Thread(() -> {
           try (InputStream in = process.getInputStream()) {
               in.transferTo(outputStream);
           } catch (Exception ignored) {}
        }, "cmd-stdout-transfer");

        Thread stdErrThread = new Thread(() -> {
            try (InputStream in = process.getErrorStream()) {
                in.transferTo(errorStream);
            } catch (Exception ignored) {}
        }, "cmd-stderr-transfer");

        stdOutThread.start();
        stdErrThread.start();
        // join threads after process is finished
        process.waitFor();
        stdOutThread.join();
        stdErrThread.join();
    }
}
