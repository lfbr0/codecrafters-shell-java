package shell;

public class PipelineCommand {

    private final String command;
    private final String[] args;
    private final String[] commandAndArgs;

    public PipelineCommand(String command, String[] args) {
        this.command = command;
        this.args = args;

        commandAndArgs = new String[args.length + 1];
        commandAndArgs[0] = command;
        System.arraycopy(args, 0, commandAndArgs, 1, args.length);
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    public String[] getCommandAndArgs() {
        return commandAndArgs;
    }
}
