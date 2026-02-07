package environment;

import command.CodeCraftersShellCommand;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.Files.isExecutable;

public class CodeCraftersShellEnvironment {

    private static CodeCraftersShellEnvironment SINGLETON_INSTANCE;
    private static final Map<String, Path> COMMANDS_IN_PATH = new HashMap<>();

    private final Map<String, CodeCraftersShellCommand> registeredCommands;
    private final List<String> history;
    private File currDirFile;
    private int historyAppendIndex;

    private CodeCraftersShellEnvironment() {
        this.registeredCommands = new HashMap<>();
        this.currDirFile = new File(".").toPath().toFile();
        this.history = new LinkedList<>();
        this.historyAppendIndex = 0;
    }

    public static synchronized CodeCraftersShellEnvironment getEnvironment() {
        if (SINGLETON_INSTANCE == null) {
            SINGLETON_INSTANCE = new CodeCraftersShellEnvironment();
        }
        return SINGLETON_INSTANCE;
    }


    /**
     * Set current working directory
     * @param path path to set working directory
     */
    public void setCurrentDirectory(String path) {
        this.currDirFile = new File(path).toPath().toFile();
    }

    /**
     * Gets current working directory
     * @return File of current directory
     */
    public File getCurrentDirectory() {
        String normalizedCurrDir = URI.create(currDirFile.getAbsolutePath())
                .normalize()
                .toString();

        if (normalizedCurrDir.endsWith(File.separator)) {
            normalizedCurrDir = normalizedCurrDir.substring(0, normalizedCurrDir.length()-1);
        }

        return new File(normalizedCurrDir);
    }

    /**
     * Register a built in command to this shell that is not searched on $PATH
     * @param commandName command to register
     * @param command command handler
     */
    public void registerBuiltinCommand(String commandName, CodeCraftersShellCommand command) {
        registeredCommands.put(commandName, command);
    }

    /**
     * Checks if command is registered as built in
     * @param command command to search
     * @return true if builtin
     */
    public boolean hasBuiltinCommand(String command) {
        return registeredCommands.containsKey(command);
    }

    /**
     * Get command handler for command
     * @param commandName command to get from shell builtin
     * @return optional command handler
     */
    public Optional<CodeCraftersShellCommand> getBuiltinCommand(String commandName) {
        return Optional.ofNullable(registeredCommands.get(commandName));
    }

    /**
     * Get all builtin commands registered in shell
     * @return list of builtin commands
     */
    public List<String> getBuiltinCommands() {
        return new ArrayList<>(registeredCommands.keySet());
    }

    /**
     * Try to find command in any of the directories of $PATH and return it
     * @param command command/executable to find
     * @return executable full path
     */
    public static Optional<Path> commandPath(String command) {
        if (COMMANDS_IN_PATH.containsKey(command)) {
            return Optional.of(COMMANDS_IN_PATH.get(command));
        }
        // it does not exist, so let's refresh cache and see if we can get it now...
        refreshCommandsInPath();
        return Optional.ofNullable(COMMANDS_IN_PATH.get(command));
    }

    /**
     * Refreshes cache of commands in $PATH
     * @implNote This method is called on first use of commandPath()
     */
    private static void refreshCommandsInPath() {
        String pathVariable = System.getenv("PATH");
        String[] pathsInPathVariable = pathVariable.split(File.pathSeparator);

        for (String path : pathsInPathVariable) {
            // get all files in path var sub path
            File[] filesInPath = new File(path).listFiles();
            if (filesInPath == null)
                continue;

            for (File file : filesInPath) {
                Path filePath = file.toPath();
                if (!isExecutable(filePath))
                    continue;

                // add to COMMANDS_PATH map if not existing for caching
                COMMANDS_IN_PATH.putIfAbsent(file.getName(), filePath);
            }
        }
    }

    /**
     * Checks if command is registered in $PATH
     * @param command command/executable to check
     * @return true if command exists in path
     */
    public boolean hasCommand(String command) {
        return commandPath(command).isPresent();
    }

    /**
     * Get command runner for command in shell
     * @param command command to run
     * @return command executor
     */
    public Optional<CodeCraftersShellCommand> getCommand(String command) {
        return commandPath(command).map(cmdPath -> {
           return (os, es, args) -> {
                // form args
                List<String> argsList = new ArrayList<>(args.length + 1);
                argsList.add(cmdPath.getFileName().toString());
                argsList.addAll(Arrays.asList(args));

                // create process at current working directory
                ProcessBuilder processBuilder = new ProcessBuilder(argsList).directory(currDirFile);
                Process process = processBuilder.start();
                process.getInputStream().transferTo(os);
                process.getErrorStream().transferTo(es);
           };
        });
    }

    /**
     * Get all commands registered in $PATH
     * @return list of commands
     */
    public List<String> getPathCommands() {
        refreshCommandsInPath();
        return new ArrayList<>(COMMANDS_IN_PATH.keySet());
    }

    /**
     * Retrieves command history (last is last executed)
     * @return list of commands
     */
    public List<String> getHistoryCopy() {
        return new ArrayList<>(history);
    }

    /**
     * Adds command to history
     * @param command command to add
     */
    public void addToHistory(String command) {
        history.add(command);
    }

    /**
     * Get index of next history line to append to
     * @return index to append to
     */
    public int getHistoryAppendIndex() {
        return historyAppendIndex;
    }

    /**
     * Set index of next history line to append to
     * @param historyAppendIndex index to set
     */
    public void setHistoryAppendIndex(int historyAppendIndex) {
        this.historyAppendIndex = historyAppendIndex;
    }

}
