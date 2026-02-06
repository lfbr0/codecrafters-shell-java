import environment.CodeCraftersShellEnvironment;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class CodeCraftersShell implements AutoCloseable {

    private final CodeCraftersShellEnvironment shellEnvironment;
    private InputStream inputStream = System.in;
    private OutputStream outputStream = System.out;
    private OutputStream errorStream  = System.err;

    // shell state vars
    private boolean shouldClose = false;

    public CodeCraftersShell(CodeCraftersShellEnvironment shellEnvironment) {
        this.shellEnvironment = shellEnvironment;
    }

    @Override
    public void close() throws Exception {
    }

    // perform REPL cycle in shell
    public void repl() {
        try (Scanner scanner = new Scanner(inputStream)) {
            // while should not close
            while (!shouldClose) {
                System.out.print("$ ");
                interpret(scanner.nextLine().trim());
            }
        }
    }

    private void interpret(String line) {
        String command = line.split(" ")[0].trim();
        String args = line.substring(command.length()).trim();

        // if exit condition, then exit shell
        if (command.equals("exit")) {
            shouldClose = true;
            return;
        }

        // find & execute command or say it's unknown
        shellEnvironment
                .getCommand(command)
                .ifPresentOrElse(
                        cmd -> cmd.execute(outputStream, errorStream, args),
                        () -> new PrintStream(outputStream).println(command + ": command not found")
                );
    }

}
