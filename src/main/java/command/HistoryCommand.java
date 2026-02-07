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
            shellEnvironment.setHistoryAppendIndex(shellEnvironment.getHistoryCopy().size());
            return;
        }

        // -w (-a does same but append) flag implies writing from history to file path in args[1] & not doing anything else
        if (args != null && args.length >= 2 && (args[0].equals("-w") || args[0].equals("-a"))) {
            int appendStart = Math.min(shellEnvironment.getHistoryAppendIndex(), history.size());

            if (args[0].equals("-a")) { // append mode
                List<String> toAppend = history.subList(appendStart, history.size());
                Files.write(
                        Path.of(args[1]),
                        toAppend,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            } else { // it's write mode
                Files.write(
                        Path.of(args[1]),
                        history,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
            }
            shellEnvironment.setHistoryAppendIndex(history.size());
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
