package completion;

import environment.CodeCraftersShellEnvironment;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.LineReaderImpl;

import java.util.AbstractList;
import java.util.List;

/**
 * This class is responsible for autocompletion when pressing TAB
 */
public class CodeCraftersShellCompleter implements Completer {

    private final List<String> allCommands;

    public CodeCraftersShellCompleter(CodeCraftersShellEnvironment env) {
        this.allCommands = new AbstractList<>() {
            @Override
            public int size() {
                return env.getBuiltinCommands().size() + env.getPathCommands().size();
            }

            @Override
            public String get(int i) {
                if (i < env.getBuiltinCommands().size()) {
                    return env.getBuiltinCommands().get(i);
                }
                return env.getPathCommands().get(i - env.getBuiltinCommands().size());
            }
        };
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        List<String> matchingCommands = allCommands.stream()
                .filter(command -> command.startsWith(line.word()))
                .toList();

        // if no matches, or more than one match, then beep
        if (matchingCommands.size() != 1) {
            ((LineReaderImpl) reader).beep();
        }

        // fill in candidates with a lazy list
        if (!matchingCommands.isEmpty()) {
            candidates.addAll(new AbstractList<>() {
                @Override
                public Candidate get(int i) {
                    return new Candidate(matchingCommands.get(i));
                }

                @Override
                public int size() {
                    return matchingCommands.size();
                }
            });
        }
    }
}
