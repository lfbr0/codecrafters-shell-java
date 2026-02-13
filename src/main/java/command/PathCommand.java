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
    public void execute(InputStream inputStream, OutputStream outputStream, OutputStream errorStream, String... args) throws Exception {
        // form args
        List<String> argsList = new ArrayList<>(args.length + 1);
        argsList.add(cmdPath.getFileName().toString());
        argsList.addAll(Arrays.asList(args));

        // create process at current working directory
        ProcessBuilder processBuilder = new ProcessBuilder(argsList).directory(currentWorkDirectoryFile);
        boolean inheritInput = inputStream == System.in;
        boolean inheritOutput = outputStream == System.out;
        boolean inheritError = errorStream == System.err;
        if (inheritInput) {
            processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        }
        if (inheritOutput) {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }
        if (inheritError) {
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        }
        Process process = processBuilder.start();

        // transfer concurrently between shell streams and process streams
        Thread stdInThread = null;
        if (!inheritInput) {
            stdInThread = new Thread(() -> {
                try (OutputStream processInputStream = process.getOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int n;
                    while ((n = inputStream.read(buffer)) != -1) {
                        processInputStream.write(buffer, 0, n);
                        processInputStream.flush();
                    }
                } catch (Exception ignored) {}
            }, "cmd-stdin-transfer");
            stdInThread.start();
        }

        Thread stdOutThread = null;
        if (!inheritOutput) {
            stdOutThread = new Thread(() -> {
               try (InputStream in = process.getInputStream()) {
                   in.transferTo(outputStream);
               } catch (Exception ignored) {}
            }, "cmd-stdout-transfer");
            stdOutThread.start();
        }

        Thread stdErrThread = null;
        if (!inheritError) {
            stdErrThread = new Thread(() -> {
                try (InputStream in = process.getErrorStream()) {
                    in.transferTo(errorStream);
                } catch (Exception ignored) {}
            }, "cmd-stderr-transfer");
            stdErrThread.start();
        }

        // join threads after process is finished
        process.waitFor();
        if (stdInThread != null) {
            stdInThread.join();
        }
        if (stdOutThread != null) {
            stdOutThread.join();
        }
        if (stdErrThread != null) {
            stdErrThread.join();
        }
    }
}
