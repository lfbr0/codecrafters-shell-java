package command;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;

public class PwdCommand implements CodeCraftersShellCommand {
    @Override
    public void execute(OutputStream outputStream, OutputStream errorStream, String... args) throws Exception {
        URI uri = URI.create(new File(".").getAbsolutePath()).normalize();
        new PrintStream(outputStream).println(uri);
    }
}
