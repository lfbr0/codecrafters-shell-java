package command;

import environment.CodeCraftersShellEnvironment;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Path;

public class CdCommand implements CodeCraftersShellCommand {

    private final CodeCraftersShellEnvironment shellEnvironment;

    public CdCommand(CodeCraftersShellEnvironment shellEnvironment) {
        this.shellEnvironment = shellEnvironment;
    }

    @Override
    public void execute(OutputStream outputStream, OutputStream errorStream, String... args) throws Exception {
        if (args == null || args.length == 0) {
            new PrintStream(errorStream).println("No directory passed to cd");
            return;
        }

        String dir = args[0];

        // replace all ~ symbol with home path
        dir = dir.replaceAll("~", shellEnvironment.getUserHomeDir());

        // if not absolute path, then apply seperators & normalize it
        if (!dir.startsWith(File.separator)) {
            dir = URI.create(shellEnvironment.getCurrentDirectory().getAbsolutePath() + File.separator + dir)
                    .normalize()
                    .toString();
        }

        if (!new File(dir).exists())
            new PrintStream(errorStream).println("cd: " + dir + ": No such file or directory");
        else
            shellEnvironment.setCurrentDirectory(dir);
    }
}
