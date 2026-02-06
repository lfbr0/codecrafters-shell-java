package command;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;

public class PwdCommand implements CodeCraftersShellCommand {
    @Override
    public void execute(OutputStream outputStream, OutputStream errorStream, String... args) throws Exception {
        String pwd = URI.create(new File(".").getAbsolutePath())
                .normalize()
                .toString();

        // trim trailing "/"
        if (pwd.endsWith(File.separator)) {
            pwd = pwd.substring(0, pwd.length()-1);
        }

        new PrintStream(outputStream).println(pwd);
    }
}
