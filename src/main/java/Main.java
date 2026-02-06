import command.EchoCommand;
import command.TypeCommand;
import environment.CodeCraftersShellEnvironment;

import static environment.CodeCraftersShellEnvironment.getEnvironment;

public class Main {
    public static void main(String[] args) throws Exception {

        // initialize classes in command package
        CodeCraftersShellEnvironment codeCraftersShellEnvironment = getEnvironment();
        codeCraftersShellEnvironment.registerBuiltinCommand("exit", (_, _, _)  ->  {});
        codeCraftersShellEnvironment.registerBuiltinCommand("echo", new EchoCommand());
        codeCraftersShellEnvironment.registerBuiltinCommand("type", new TypeCommand());

        try (CodeCraftersShell shell = new CodeCraftersShell(getEnvironment())) {
            shell.repl();
        }

    }
}
