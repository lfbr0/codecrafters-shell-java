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
        List<String> history = shellEnvironment.getHistory();

        Stream<String> historyStream = IntStream
                .range(0, history.size()) // enumerate from 0 .. history.length
                .mapToObj(i -> String.format("\t%d %s\n", i+1, history.get(i)));

        // if there is arguments, and first is number of history lines
        if (args != null && args.length >= 1 && args[0].matches("[0-9]+")) {
            int limit = Integer.parseInt(args[0]);
            historyStream = historyStream.limit(limit);
        }

        // for each, print them
        PrintStream printStream = new PrintStream(outputStream);
        historyStream.forEach(printStream::print);
    }
}
