package command;

import java.io.OutputStream;
import java.io.PrintStream;

import static environment.CodeCraftersShellEnvironment.getEnvironment;

public class TypeCommand implements CodeCraftersShellCommand {
    @Override
    public void execute(OutputStream outputStream, OutputStream errorStream, String... args) {
        PrintStream printStream = new PrintStream(outputStream);
        PrintStream printStreamError = new PrintStream(errorStream);

        for (String arg : args) {
            if (getEnvironment().hasCommand(arg)) {
                printStream.println(arg + " is a shell builtin");
            } else {
                printStreamError.println(arg + ": not found");
            }
        }

    }
}
