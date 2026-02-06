package environment;

import command.CodeCraftersShellCommand;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CodeCraftersShellEnvironment {

    private static CodeCraftersShellEnvironment SINGLETON_INSTANCE;
    private final Map<String, CodeCraftersShellCommand> registeredCommands;

    private CodeCraftersShellEnvironment() {
        this.registeredCommands = new ConcurrentHashMap<>();
    }

    public static synchronized CodeCraftersShellEnvironment getEnvironment() {
        if (SINGLETON_INSTANCE == null) {
            SINGLETON_INSTANCE = new CodeCraftersShellEnvironment();
        }
        return SINGLETON_INSTANCE;
    }

    public void registerBuiltinCommand(String commandName, CodeCraftersShellCommand command) {
        registeredCommands.put(commandName, command);
    }

    public Optional<CodeCraftersShellCommand> getBuiltinCommand(String commandName) {
        return Optional.ofNullable(registeredCommands.get(commandName));
    }

    public boolean hasBuiltinCommand(String arg) {
        return registeredCommands.containsKey(arg);
    }
}
