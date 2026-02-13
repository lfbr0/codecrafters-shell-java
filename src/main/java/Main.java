import command.*;
import completion.CodeCraftersShellCompleter;
import environment.CodeCraftersShellEnvironment;
import shell.CodeCraftersShell;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static environment.CodeCraftersShellEnvironment.getEnvironment;

public class Main {
    public static void main(String[] args) throws Exception {
        // initialize classes in command package
        CodeCraftersShellEnvironment shellEnvironment = getEnvironment();
        shellEnvironment.registerBuiltinCommand("exit", (_, _, _, _)  ->  {});
        shellEnvironment.registerBuiltinCommand("echo", new EchoCommand(shellEnvironment));
        shellEnvironment.registerBuiltinCommand("type", new TypeCommand(shellEnvironment));
        shellEnvironment.registerBuiltinCommand("pwd", new PwdCommand(shellEnvironment));
        shellEnvironment.registerBuiltinCommand("cd", new CdCommand(shellEnvironment));

        // if history env variable is set, then read into memory history file & then register it
        HistoryCommand historyCommand = new HistoryCommand(shellEnvironment);
        Optional<String> histFileOptional = Optional.ofNullable(System.getenv("HISTFILE"));
        if (histFileOptional.isPresent()) {
            try {
                historyCommand.readHistoryFromFile(histFileOptional.get());
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
        shellEnvironment.registerBuiltinCommand("history", historyCommand);

        // start repl here!
        CodeCraftersShellEnvironment env = getEnvironment();
        new CodeCraftersShell(env, new CodeCraftersShellCompleter(env)).repl();

        // if history file exists, then write history to it on closing
        if (histFileOptional.isPresent()) {
            boolean appendIfExists = Files.exists(Path.of(histFileOptional.get()));
            historyCommand.writeHistoryToFile(appendIfExists, shellEnvironment.getHistoryCopy(), histFileOptional.get());
        }
    }
}
