import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class CodeCraftersShell implements AutoCloseable {

    private InputStream inputStream = System.in;
    private OutputStream outputStream = System.out;
    private OutputStream errorStream  = System.err;

    @Override
    public void close() throws Exception {
    }

    public void fetchNextLineAndInterpret() {
        try (Scanner scanner = new Scanner(inputStream)) {
            String line = scanner.nextLine();
            interpret(line);
        }
    }

    private void interpret(String line) {
        try (PrintStream out = new PrintStream(outputStream)) {
            out.println(line + ": command not found");
        }
    }

}
