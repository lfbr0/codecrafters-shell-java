import command.EchoCommand;
import environment.CodeCraftersShellEnvironment;

import static environment.CodeCraftersShellEnvironment.getEnvironment;

public class Main {
    public static void main(String[] args) throws Exception {

        // initialize classes in command package
        Class.forName(EchoCommand.class.getName());

        try (CodeCraftersShell shell = new CodeCraftersShell(getEnvironment())) {
            shell.repl();
        }

    }
}
