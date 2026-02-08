package shell;

import completion.CodeCraftersShellCompleter;
import environment.CodeCraftersShellEnvironment;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
        String commandArgsLine = line.trim();
        CommandParseResult parsedCommandAndArgs = CommandParseResult.parse(commandArgsLine);
        String command = parsedCommandAndArgs.getCommand();
        String[] args = parsedCommandAndArgs.getArgs();

        // if exit condition, then exit shell
        if (command.equals("exit")) {
            shouldClose = true;
            return;
        }

        // determine streams to use - by default, stdout & stderr
        OutputStream outputStreamToUse = outputStream;
        OutputStream errorStreamToUse = errorStream;

        try {
            int redirectionIndex = -1;
            // check if redirection argument of stdout
            if (
                    (redirectionIndex = parsedCommandAndArgs.argumentIndex(">"))    != -1     ||
                    (redirectionIndex = parsedCommandAndArgs.argumentIndex("1>"))   != -1     ||
                    (redirectionIndex = parsedCommandAndArgs.argumentIndex(">>"))   != -1     ||
                    (redirectionIndex = parsedCommandAndArgs.argumentIndex("1>>"))  != -1
            ) {
                String filePath = args[redirectionIndex + 1];
                boolean append = args[redirectionIndex].equals(">>") || args[redirectionIndex].equals("1>>");
                outputStreamToUse = new FileOutputStream(filePath, append);
            }
            // check if redirection argument of stderr
            else if (
                    (redirectionIndex = parsedCommandAndArgs.argumentIndex("2>")) != -1
            ) {
                String filePath = args[redirectionIndex + 1];
                errorStreamToUse = new FileOutputStream(filePath);
            }

            // remove from args array if redirection
            if (redirectionIndex != -1) {
                args = new ArrayList<>(List.of(args))
                        .subList(0, redirectionIndex)
                        .toArray(String[]::new);
            }

            // if builtin command, then execute it (get from shell env)
            if (shellEnvironment.hasBuiltinCommand(command)) {
                shellEnvironment
                        .getBuiltinCommand(command)
                        .get()
                        .execute(outputStreamToUse, errorStreamToUse, args);
                return;
            }

            // try to execute command from path var
            if (shellEnvironment.hasCommand(command)) {
                shellEnvironment
                        .getCommand(command)
                        .get()
                        .execute(outputStreamToUse, errorStreamToUse, args);
                return;
            }

            // no command has been found!
            new PrintStream(outputStreamToUse).println(command + ": command not found");
        } catch (Exception e) {
            // print stack trace and exit
            e.printStackTrace(new PrintStream(errorStreamToUse));
            shouldClose = true;
        }
    }
}
