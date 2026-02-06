import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        try (CodeCraftersShell shell = new CodeCraftersShell()) {
            System.out.print("$ ");
            shell.fetchNextLineAndInterpret();
        }

    }
}
