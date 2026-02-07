package command;

import environment.CodeCraftersShellEnvironment;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class HistoryCommand implements CodeCraftersShellCommand {

    private final CodeCraftersShellEnvironment shellEnvironment;

    public HistoryCommand(CodeCraftersShellEnvironment shellEnvironment) {
        this.shellEnvironment = shellEnvironment;
    }

    @Override
    public void execute(OutputStream outputStream, OutputStream errorStream, String... args) throws Exception {
        // if there are arguments, and first is number of history lines
        int start = 0;
        if (args != null && args.length >= 1 && args[0].matches("[0-9]+")) {
            start = Integer.parseInt(args[0]);
        }

        List<String> history = shellEnvironment.getHistory();
        PrintStream printStream = new PrintStream(outputStream);

        for (int i = start; i < history.size(); i++) {
            printStream.printf("\t%d %s\n", i+1, history.get(i));
        }
    }
}
