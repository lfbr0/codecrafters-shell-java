package command;

import environment.CodeCraftersShellEnvironment;

import java.io.OutputStream;
import java.io.PrintStream;

import static environment.CodeCraftersShellEnvironment.getEnvironment;

public class TypeCommand implements CodeCraftersShellCommand {

    private final CodeCraftersShellEnvironment shellEnvironment;

    public TypeCommand(CodeCraftersShellEnvironment shellEnvironment) {
        this.shellEnvironment = shellEnvironment;
    }

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
