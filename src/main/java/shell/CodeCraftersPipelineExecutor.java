package shell;

import environment.CodeCraftersShellEnvironment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeCraftersPipelineExecutor {

    private final CodeCraftersShell shell;
    private final CodeCraftersShellEnvironment environment;
    private final CommandParseResult parsedCommand;

    public CodeCraftersPipelineExecutor(CodeCraftersShell shell,
                                        CodeCraftersShellEnvironment environment,
                                        CommandParseResult parsedCommand) {
        this.shell = shell;
        this.environment = environment;
        this.parsedCommand = parsedCommand;
    }

    public void executePipeline() throws IOException, InterruptedException {
        List<PipelineCommand> pipelineCommands = buildPipelineCommands();
        List<ProcessBuilder> processBuilders = new ArrayList<>(pipelineCommands.size());

        for (int i = 0; i < pipelineCommands.size(); i++) {
            ProcessBuilder pb = new ProcessBuilder(pipelineCommands.get(i).getCommandAndArgs())
                    .directory(environment.getCurrentDirectory());

            if (i != pipelineCommands.size() - 1) {
                pb = pb
                        .inheritIO()
                        .redirectError(ProcessBuilder.Redirect.PIPE)
                        .redirectInput(ProcessBuilder.Redirect.PIPE)
                        .redirectOutput(ProcessBuilder.Redirect.PIPE);
            } else {
                pb = pb.redirectError(ProcessBuilder.Redirect.INHERIT)
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT);
            }

            processBuilders.add(pb);
        }

        List<Process> processes = ProcessBuilder.startPipeline(processBuilders);
        for (Process process : processes) process.waitFor();
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
