import command.EchoCommand;
import command.TypeCommand;
import environment.CodeCraftersShellEnvironment;

import static environment.CodeCraftersShellEnvironment.getEnvironment;

public class Main {
    public static void main(String[] args) throws Exception {

        // initialize classes in command package
        CodeCraftersShellEnvironment codeCraftersShellEnvironment = getEnvironment();
        codeCraftersShellEnvironment.registerCommand("echo", new EchoCommand());
        codeCraftersShellEnvironment.registerCommand("type", new TypeCommand());

        try (CodeCraftersShell shell = new CodeCraftersShell(getEnvironment())) {
            shell.repl();
        }

    }
}
