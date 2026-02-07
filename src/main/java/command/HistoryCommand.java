package command;

import environment.CodeCraftersShellEnvironment;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class HistoryCommand implements CodeCraftersShellCommand {

    private final CodeCraftersShellEnvironment shellEnvironment;

    public HistoryCommand(CodeCraftersShellEnvironment shellEnvironment) {
        this.shellEnvironment = shellEnvironment;
    }

    @Override
    public void execute(OutputStream outputStream, OutputStream errorStream, String... args) throws Exception {
        PrintStream printStream = new PrintStream(outputStream);
        for (int i = 0; i < shellEnvironment.getHistory().size(); i++) {
            printStream.printf("\t%d %s\n", i+1, shellEnvironment.getHistory().get(i));
        }
    }
}
