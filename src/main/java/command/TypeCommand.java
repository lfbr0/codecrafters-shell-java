package command;

import environment.CodeCraftersShellEnvironment;

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
            CodeCraftersShellEnvironment
                    .commandPath(arg)
                    .ifPresentOrElse(
                            commandPath ->  printStream.println(arg + " is " + commandPath),
                            () -> printStreamError.println(arg + ": not found")
            );
        }
    }
}
