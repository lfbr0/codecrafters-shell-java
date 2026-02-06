package command;

import java.io.OutputStream;

@FunctionalInterface
public interface CodeCraftersShellCommand {
    void execute(OutputStream outputStream, OutputStream errorStream, String ... args) throws Exception;
}
