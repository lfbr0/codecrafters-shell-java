package completion;

import environment.CodeCraftersShellEnvironment;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.List;

/**
 * This class is responsible for autocompletion when pressing TAB
 */
public class CodeCraftersShellCompleter implements Completer {

    private final CodeCraftersShellEnvironment env;
    private final StringsCompleter stringsCompleter;

    public CodeCraftersShellCompleter(CodeCraftersShellEnvironment env) {
        this.env = env;
        this.stringsCompleter = new StringsCompleter(env.getBuiltinCommands());
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        stringsCompleter.complete(reader, line, candidates);
    }
}
