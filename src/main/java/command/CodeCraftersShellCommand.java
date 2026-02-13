package command;

import java.io.InputStream;
import java.io.OutputStream;

@FunctionalInterface
public interface CodeCraftersShellCommand {
    void execute(InputStream inputStream, OutputStream outputStream, OutputStream errorStream, String... args) throws Exception;
}
