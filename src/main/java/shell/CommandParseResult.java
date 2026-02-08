package shell;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of parsing a user input line into a command and its arguments.
 *
 * This class is immutable and thread-safe. Use {@link #parse(String)} to
 * construct an instance from a raw input line.
 */
public final class CommandParseResult {
    private final String command;
    private final String[] args;
    private final int consumed; // chars consumed from the trimmed line (includes quotes if present)

    public CommandParseResult(String command, int consumed, String[] args) {
        this.command = command == null ? "" : command;
        this.consumed = Math.max(0, consumed);
        this.args = args == null ? new String[0] : args.clone();
    }

    public static CommandParseResult parse(String commandArgsLine) {
        if (commandArgsLine == null) return new CommandParseResult("", 0, new String[0]);

        String line = commandArgsLine.trim();
        if (line.isEmpty()) return new CommandParseResult("", 0, new String[0]);

        Token first = readToken(line, 0);
        String cmd = first.value;

        String argsPart = (first.end < line.length()) ? line.substring(first.end).trim() : "";
        List<Token> tokens = tokenizeWithPositions(argsPart);

        // Recombine tokens into arguments: adjacent tokens (no whitespace between) are concatenated into one argument
        List<String> argList = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int prevEnd = -1;
        for (Token t : tokens) {
            if (prevEnd != -1 && t.start == prevEnd) {
                // contiguous token -> append to current arg
                current.append(t.value);
            } else {
                // new arg
                if (!current.isEmpty()) {
                    argList.add(current.toString());
                }
                current.setLength(0);
                current.append(t.value);
            }
            prevEnd = t.end;
        }
        if (!current.isEmpty()) argList.add(current.toString());

        String[] parsedArgs = argList.toArray(new String[0]);

        return new CommandParseResult(cmd, first.end, parsedArgs);
    }

    // ---- Tokenization (shared by command + args) ----

    private static final class Token {
        final String value;
        final int start; // inclusive index where token started in the input
        final int end;   // index in the input string where parsing stopped (>= start)
        Token(String value, int start, int end) { this.value = value; this.start = start; this.end = end; }
    }

    /**
     * Tokenize but keep token start positions. Tokens are separated by whitespace.
     */
    private static List<Token> tokenizeWithPositions(String s) {
        List<Token> out = new ArrayList<>();
        if (s == null || s.isEmpty()) return out;

        int i = 0;
        while (i < s.length()) {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
            if (i >= s.length()) break;

            int start = i;
            Token t = readToken(s, i);
            if (!t.value.isEmpty()) out.add(new Token(t.value, start, t.end));
            i = t.end;
        }
        return out;
    }

    /**
     * Reads a single shell-like token starting at 'start'.
     * - Outside quotes: backslash escapes next char (or is literal if trailing)
     * - Single quotes: everything literal, backslash is literal
     * - Double quotes: only \" and \\\\ are unescaped; other backslashes stay as '\\'
     * Returns token value (without quote chars) and end index (includes closing quote if present).
     */
    private static Token readToken(String s, int start) {
        StringBuilder sb = new StringBuilder();
        int i = start;

        if (i >= s.length()) return new Token("", i, i);

        char first = s.charAt(i);
        boolean inSingle = first == '\'';
        boolean inDouble = first == '"';

        if (inSingle || inDouble) {
            char quote = first;
            i++; // consume opening quote

            while (i < s.length()) {
                char c = s.charAt(i);

                if (c == '\\') {
                    if (inSingle) { // literal backslash
                        sb.append('\\');
                        i++;
                        continue;
                    }
                    // double quotes
                    if (i + 1 >= s.length()) { sb.append('\\'); i++; continue; }
                    char next = s.charAt(i + 1);
                    if (next == '"' || next == '\\') { sb.append(next); i += 2; }
                    else { sb.append('\\'); i++; }
                    continue;
                }

                if (c == quote) { // closing quote
                    i++; // include closing quote in consumed/end
                    break;
                }

                sb.append(c);
                i++;
            }

            return new Token(sb.toString(), start, i);
        }

        // Unquoted token
        while (i < s.length()) {
            char c = s.charAt(i);

            if (Character.isWhitespace(c)) break;

            if (c == '\\') {
                if (i + 1 < s.length()) { sb.append(s.charAt(i + 1)); i += 2; }
                else { sb.append('\\'); i++; }
                continue;
            }

            // Handle embedded quoted sections inside an unquoted token
            if (c == '\'' || c == '"') {
                char quote = c;
                boolean innerDouble = quote == '"';
                i++; // consume opening quote
                while (i < s.length()) {
                    char qc = s.charAt(i);
                    if (qc == '\\') {
                        if (!innerDouble) {
                            sb.append('\\');
                            i++;
                            continue;
                        }
                        if (i + 1 >= s.length()) { sb.append('\\'); i++; break; }
                        char next = s.charAt(i + 1);
                        if (next == '"' || next == '\\') { sb.append(next); i += 2; }
                        else { sb.append('\\'); i++; }
                        continue;
                    }
                    if (qc == quote) { i++; break; }
                    sb.append(qc);
                    i++;
                }
                continue;
            }

            sb.append(c);
            i++;
        }

        return new Token(sb.toString(), start, i);
    }

    public String getCommand() { return command; }
    public String[] getArgs() { return args.clone(); }
    public int getConsumed() { return consumed; }
}
