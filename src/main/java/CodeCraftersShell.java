import completion.CodeCraftersShellCompleter;
import environment.CodeCraftersShellEnvironment;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeCraftersShell implements AutoCloseable {

    private final CodeCraftersShellEnvironment shellEnvironment;
    private final CodeCraftersShellCompleter shellCompleter;
    private InputStream inputStream = System.in;
    private OutputStream outputStream = System.out;
    private OutputStream errorStream  = System.err;

    // shell state vars
    private boolean shouldClose = false;

    public CodeCraftersShell(CodeCraftersShellEnvironment shellEnvironment,
                             CodeCraftersShellCompleter shellCompleter) {
        this.shellEnvironment = shellEnvironment;
        this.shellCompleter = shellCompleter;
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
        outputStream.close();
        errorStream.close();
    }

    /**
     * Starts the shell REPL
     * @throws IOException
     */
    public void repl() throws IOException {
        DefaultParser parser = new DefaultParser();
        parser.setEscapeChars(new char[0]);

        try (Terminal terminal = TerminalBuilder.builder()
                .streams(inputStream, outputStream)
                .system(true)
                .build()
        ) {
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .parser(parser)
                    .completer(shellCompleter)
                    .option(LineReader.Option.INSERT_TAB, true)
                    .option(LineReader.Option.AUTO_LIST, false)
                    .option(LineReader.Option.AUTO_MENU, false)
                    .option(LineReader.Option.MENU_COMPLETE, false)
                    .build();

            // add history to reader
            shellEnvironment.getHistoryCopy().forEach(cmd -> reader.getHistory().add(cmd));

            while (!shouldClose) {
                String line = reader.readLine("$ ");
                if (line == null) {
                    continue;
                }

                line = line.trim();
                if (!line.isEmpty()) {
                    shellEnvironment.addToHistory(line);
                }

                interpret(line);
                terminal.flush();
            }
        }
    }

    /**
     * Interpret a line of input from the user and execute it
     * @param line line of input from user
     */
    private void interpret(String line) {
        // get command and args
        String command = line.split(" ")[0].trim();
        String[] args = parseArguments(line.substring(command.length()).trim());

        // if exit condition, then exit shell
        if (command.equals("exit")) {
            shouldClose = true;
            return;
        }

        try {
            // if builtin command, then execute it (get from shell env)
            if (shellEnvironment.hasBuiltinCommand(command)) {
                shellEnvironment
                        .getBuiltinCommand(command)
                        .get()
                        .execute(outputStream, errorStream, args);
                return;
            }

            // try to execute command from path var
            if (shellEnvironment.hasCommand(command)) {
                shellEnvironment
                        .getCommand(command)
                        .get()
                        .execute(outputStream, errorStream, args);
                return;
            }

            // no command has been found!
            new PrintStream(outputStream).println(command + ": command not found");
        } catch (Exception e) {
            // print stack trace and exit
            e.printStackTrace(new PrintStream(errorStream));
            shouldClose = true;
        }
    }

    /**
     * From the input line of the user, parse out the arguments
     * @param argsLine raw input line of user
     * @return array of arguments
     */
    private String[] parseArguments(String argsLine) {
        List<String> argsArray = new LinkedList<>();
        StringBuilder currentArg = new StringBuilder();

        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;

        for (int i = 0; i < argsLine.length(); i++) {
            char c = argsLine.charAt(i);

            // Backslash escaping (ONLY outside quotes)
            if (c == '\\' && !inSingleQuotes && !inDoubleQuotes) {
                if (i + 1 < argsLine.length()) {
                    // Escape next char: add it literally, drop the backslash
                    currentArg.append(argsLine.charAt(i + 1));
                    i++; // skip next char (already consumed)
                } else {
                    // Trailing '\' with nothing to escape: keep it literal (reasonable fallback)
                    currentArg.append('\\');
                }
                continue;
            }

            // Quote toggles (only if not inside the other quote type)
            if (c == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
                continue; // do not include quote char
            }

            if (c == '"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes;
                continue; // do not include quote char
            }

            // Argument separator (whitespace outside quotes)
            if (Character.isWhitespace(c) && !inSingleQuotes && !inDoubleQuotes) {
                String currentArgStr = currentArg.toString().trim();

                if (!currentArgStr.isEmpty()) {
                    argsArray.add(currentArgStr);
                }
                currentArg.setLength(0);

                // consume consecutive whitespace (shell-like collapsing)
                while (i + 1 < argsLine.length()
                        && Character.isWhitespace(argsLine.charAt(i + 1))) {
                    i++;
                }
                continue;
            }

            currentArg.append(c);
        }

        // Commit last arg
        String last = currentArg.toString().trim();
        if (!last.isEmpty()) {
            argsArray.add(last);
        }

        return argsArray.toArray(new String[0]);
    }
}
