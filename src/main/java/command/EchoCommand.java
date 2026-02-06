package command;

import environment.CodeCraftersShellEnvironment;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static environment.CodeCraftersShellEnvironment.getEnvironment;

public class EchoCommand implements CodeCraftersShellCommand {
    static {
        getEnvironment().registerCommand("echo", new EchoCommand());
    }

    @Override
    public void execute(OutputStream outputStream, OutputStream errorStream, String ... args) {
        new PrintStream(outputStream).println(String.join(" ", args));
    }
}
