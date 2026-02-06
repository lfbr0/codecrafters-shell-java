package environment;

import command.CodeCraftersShellCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CodeCraftersShellEnvironment {

    private static CodeCraftersShellEnvironment SINGLETON_INSTANCE;
    private final Map<String, CodeCraftersShellCommand> registeredCommands;

    private CodeCraftersShellEnvironment() {
        this.registeredCommands = new HashMap<>();
    }

    public void registerCommand(String commandName, CodeCraftersShellCommand command) {
        synchronized (registeredCommands) {
            registeredCommands.put(commandName, command);
        }
    }

    public Optional<CodeCraftersShellCommand> getCommand(String commandName) {
        synchronized (registeredCommands) {
            return Optional.ofNullable(registeredCommands.get(commandName));
        }
    }

    public static synchronized CodeCraftersShellEnvironment getEnvironment() {
        if (SINGLETON_INSTANCE == null) {
            SINGLETON_INSTANCE = new CodeCraftersShellEnvironment();
        }
        return SINGLETON_INSTANCE;
    }

}
