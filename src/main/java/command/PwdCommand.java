package command;

import environment.CodeCraftersShellEnvironment;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class PwdCommand implements CodeCraftersShellCommand {

    private final CodeCraftersShellEnvironment shellEnvironment;

    public PwdCommand(CodeCraftersShellEnvironment shellEnvironment) {
        this.shellEnvironment = shellEnvironment;
    }

    @Override
    public void execute(InputStream inputStream, OutputStream outputStream, OutputStream errorStream, String... args) throws Exception {
        new PrintStream(outputStream).println(shellEnvironment.getCurrentDirectory());
    }
}
