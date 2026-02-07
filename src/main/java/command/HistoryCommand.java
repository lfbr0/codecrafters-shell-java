package command;

import environment.CodeCraftersShellEnvironment;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class HistoryCommand implements CodeCraftersShellCommand {

    private final CodeCraftersShellEnvironment shellEnvironment;

    public HistoryCommand(CodeCraftersShellEnvironment shellEnvironment) {
        this.shellEnvironment = shellEnvironment;
    }

    @Override
    public void execute(OutputStream outputStream, OutputStream errorStream, String... args) throws Exception {
        // if there are arguments, and first is the number of history lines
        List<String> history = shellEnvironment.getHistoryCopy();
        PrintStream printStream = new PrintStream(outputStream);

        // -r flag implies reading from line in args[1] & not doing anything else
        if (args != null && args.length >= 2 && args[0].equals("-r")) {
            Files
                    .readAllLines(Path.of(args[1]))
                    .forEach(line -> {
                        line = line.trim();
                        if (!line.isBlank()) shellEnvironment.addToHistory(line);
                    });
            return;
        }

        // -w (-a does same but append) flag implies writing from history to file path in args[1] & not doing anything else
        if (args != null && args.length >= 2 && (args[0].equals("-w") || args[0].equals("-a"))) {
            if (args[0].equals("-a")) {
                Files.write(Path.of(args[1]), history, StandardOpenOption.APPEND);
            } else { //it's write
                Files.write(Path.of(args[1]), history);
            }
            return;
        }

        // if argument is number, then limit
        int start = 0;
        if (args != null && args.length >= 1 && args[0].matches("[0-9]+")) {
            int limit = Integer.parseInt(args[0]);
            start = Math.max(history.size() - limit, 0);
        }

        for (int i = start; i < history.size(); i++) {
            printStream.printf("\t%d %s\n", i + 1, history.get(i));
        }
    }
}
