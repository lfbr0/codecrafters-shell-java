package command;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import static environment.CodeCraftersShellEnvironment.getEnvironment;
import static java.nio.file.Files.isExecutable;
import static java.util.Arrays.stream;

public class TypeCommand implements CodeCraftersShellCommand {
    @Override
    public void execute(OutputStream outputStream, OutputStream errorStream, String... args) {
        PrintStream printStream = new PrintStream(outputStream);
        PrintStream printStreamError = new PrintStream(errorStream);

        for (String arg : args) {
            if (getEnvironment().hasBuiltinCommand(arg)) {
                printStream.println(arg + " is a shell builtin");
                return;
            }

            // check if command is in path, otherwise it doesn't exist
            commandPath(arg).ifPresentOrElse(
                    commandPath ->  printStream.println(arg + " is " + commandPath),
                    () -> printStreamError.println(arg + ": not found")
            );
        }
    }

    /**
     * Try to find command in any of the directories of $PATH and return it
     * @param command command/executable to find
     * @return executable full path
     */
    private Optional<String> commandPath(String command) {
        String pathVariable = System.getenv("PATH");
        String[] pathsInPathVariable = pathVariable.split(File.pathSeparator);

        for (String path : pathsInPathVariable) {
            // get all files in path var sub path
            File[] filesInPath = new File(path).listFiles();
            if (filesInPath == null)
                continue;

            for (File file : filesInPath) {
                if (!file.getName().equals(command))
                    continue;

                Path filePath = file.toPath();
                if (!isExecutable(filePath))
                    continue;

                return Optional.of(file.getAbsolutePath());
            }
        }

        // no match found
        return Optional.empty();
    }
}
