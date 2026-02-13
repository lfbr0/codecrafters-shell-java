package shell;

import command.CodeCraftersShellCommand;
import environment.CodeCraftersShellEnvironment;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CodeCraftersPipelineExecutor {

    private final CodeCraftersShellEnvironment environment;
    private final CommandParseResult parsedCommand;

    public CodeCraftersPipelineExecutor(CodeCraftersShellEnvironment environment,
                                        CommandParseResult parsedCommand) {
        this.environment = environment;
        this.parsedCommand = parsedCommand;
    }

    public void executePipeline(InputStream inputStream,
                                OutputStream outputStream,
                                OutputStream errorStream) throws Exception {

        List<PipelineCommand> pipelineCommands = buildPipelineCommands();
        List<CodeCraftersShellCommand> executableCommands = new ArrayList<>(pipelineCommands.size());

        for (PipelineCommand pipelineCommand : pipelineCommands) {
            Optional<CodeCraftersShellCommand> commandOptional = environment
                    .resolveCommand(pipelineCommand.getCommand());

            if (commandOptional.isEmpty()) {
                new PrintStream(outputStream).println(pipelineCommand.getCommand() + ": command not found");
                return;
            }

            executableCommands.add(commandOptional.get());
        }

        executeCommandsInPipeline(executableCommands, pipelineCommands, inputStream, outputStream, errorStream);
    }

    private void executeCommandsInPipeline(List<CodeCraftersShellCommand> executableCommands,
                                           List<PipelineCommand> pipelineCommands,
                                           InputStream inputStream,
                                           OutputStream outputStream,
                                           OutputStream errorStream) throws Exception {
        InputStream[] stageInputs = new InputStream[executableCommands.size()];
        OutputStream[] stageOutputs = new OutputStream[executableCommands.size()];

        stageInputs[0] = inputStream;
        stageOutputs[executableCommands.size() - 1] = outputStream;

        for (int i = 0; i < executableCommands.size() - 1; i++) {
            PipedOutputStream stageOutput = new PipedOutputStream();
            PipedInputStream nextInput = new PipedInputStream(stageOutput);
            stageOutputs[i] = stageOutput;
            stageInputs[i + 1] = nextInput;
        }

        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());
        List<Thread> threads = new ArrayList<>(executableCommands.size());

        for (int i = 0; i < executableCommands.size(); i++) {
            final int stageIndex = i;
            Thread stageThread = new Thread(() -> {
                try {
                    executableCommands
                            .get(stageIndex)
                            .execute(
                                    stageInputs[stageIndex],
                                    stageOutputs[stageIndex],
                                    errorStream,
                                    pipelineCommands.get(stageIndex).getArgs()
                            );
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    closeIfPipe(stageOutputs[stageIndex]);
                    closeIfPipe(stageInputs[stageIndex]);
                }
            }, "pipeline-stage-" + stageIndex);
            threads.add(stageThread);
            stageThread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        if (!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }
    }

    private void closeIfPipe(Closeable closeable) {
        if (closeable instanceof PipedInputStream || closeable instanceof PipedOutputStream) {
            try {
                closeable.close();
            } catch (IOException ignored) {}
        }
    }

    private List<PipelineCommand> buildPipelineCommands() {
        List<PipelineCommand> pipelineCommands = new ArrayList<>(parsedCommand.getArgs().length + 1);

        // construct first pipeline command
        int argIdx = parsedCommand.argumentIndex("|");
        PipelineCommand firstPipelineCommand = new PipelineCommand(
                parsedCommand.getCommand(),
                Arrays.copyOfRange(parsedCommand.getArgs(), 0, argIdx)
        );
        pipelineCommands.add(firstPipelineCommand);

        // construct remaining pipeline commands by concatening args until we reach pipe and clear
        String command = null;
        List<String> args = new ArrayList<>(parsedCommand.getArgs().length);
        for (int i = argIdx + 1; i < parsedCommand.getArgs().length; i++) {
            // reset on pipe
            if (parsedCommand.getArgs()[i].equals("|")) {
                pipelineCommands.add(new PipelineCommand(command, args.toArray(String[]::new)));
                command = null;
                args.clear();
            }
            // if no command, then set command to next arg
            else if (command == null) {
                command = parsedCommand.getArgs()[i];
            }
            // otherwise, append to args
            else {
                args.add(parsedCommand.getArgs()[i]);
            }
        }
        // add last command
        pipelineCommands.add(new PipelineCommand(command, args.toArray(String[]::new)));

        return pipelineCommands;
    }
}
