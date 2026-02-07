import completion.CodeCraftersShellCompleter;
import environment.CodeCraftersShellEnvironment;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

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

    // perform REPL cycle in shell
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

    private void interpret(String line) {
        // get command and args
        String command = line.split(" ")[0].trim();
        String[] args = line.substring(command.length()).trim().split(" ");

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

}
