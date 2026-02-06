import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class CodeCraftersShell implements AutoCloseable {

    private InputStream inputStream = System.in;
    private OutputStream outputStream = System.out;
    private OutputStream errorStream  = System.err;

    // shell state vars
    private boolean shouldClose;

    @Override
    public void close() throws Exception {
    }

    // perform REPL cycle in shell
    public void repl() {
        try (Scanner scanner = new Scanner(inputStream)) {
            // while should not close
            while (!shouldClose) {
                System.out.print("$ ");
                interpret(scanner.nextLine());
            }
        }
    }

    private void interpret(String line) {
        new PrintStream(outputStream).println(line + ": command not found");
    }

}
