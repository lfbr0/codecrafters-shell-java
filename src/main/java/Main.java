import command.CdCommand;
import command.EchoCommand;
import command.PwdCommand;
import command.TypeCommand;
import environment.CodeCraftersShellEnvironment;

import static environment.CodeCraftersShellEnvironment.getEnvironment;

public class Main {
    public static void main(String[] args) throws Exception {
        // initialize classes in command package
        CodeCraftersShellEnvironment shellEnvironment = getEnvironment();
        shellEnvironment.registerBuiltinCommand("exit", (_, _, _)  ->  {});
        shellEnvironment.registerBuiltinCommand("echo", new EchoCommand());
        shellEnvironment.registerBuiltinCommand("type", new TypeCommand());
        shellEnvironment.registerBuiltinCommand("pwd", new PwdCommand(shellEnvironment));
        shellEnvironment.registerBuiltinCommand("cd", new CdCommand(shellEnvironment));

        try (CodeCraftersShell shell = new CodeCraftersShell(getEnvironment())) {
            shell.repl();
        }
    }
}
