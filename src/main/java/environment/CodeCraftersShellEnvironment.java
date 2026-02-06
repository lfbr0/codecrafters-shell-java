package environment;

import command.CodeCraftersShellCommand;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.Files.isExecutable;

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

    public boolean hasBuiltinCommand(String arg) {
        return registeredCommands.containsKey(arg);
    }

    public Optional<CodeCraftersShellCommand> getBuiltinCommand(String commandName) {
        return Optional.ofNullable(registeredCommands.get(commandName));
    }

    /**
     * Try to find command in any of the directories of $PATH and return it
     * @param command command/executable to find
     * @return executable full path
     */
    public static Optional<Path> commandPath(String command) {
        String pathVariable = System.getenv("PATH");
        String[] pathsInPathVariable = pathVariable.split(File.pathSeparator);

        for (String path : pathsInPathVariable) {
            // get all files in path var sub path
            File[] filesInPath = new File(path).listFiles();
            if (filesInPath == null)
                continue;

            for (File file : filesInPath) {
                if (!file.getName().equals(command))
                    continue;

                Path filePath = file.toPath();
                if (!isExecutable(filePath))
                    continue;

                return Optional.of(filePath);
            }
        }

        // no match found
        return Optional.empty();
    }

    public boolean hasCommand(String command) {
        return commandPath(command).isPresent();
    }

    public Optional<CodeCraftersShellCommand> getCommand(String command) {
        return commandPath(command).map(cmdPath -> {
           return (os, es, args) -> {
                // form args
                List<String> argsList = new ArrayList<>(args.length + 1);
                argsList.add(cmdPath.getFileName().toString());
                argsList.addAll(Arrays.asList(args));

                ProcessBuilder processBuilder = new ProcessBuilder(argsList);
                Process process = processBuilder.start();
                process.getInputStream().transferTo(os);
                process.getErrorStream().transferTo(es);
           };
        });
    }
}
