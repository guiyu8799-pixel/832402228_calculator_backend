package com.fzu.calculator.expression;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps user friendly input to the canonical expression understood by the {@link Lexer}.
 *
 * <p>The front end displays the operators as '×' and '÷', and Chinese input methods produce full
 * width characters. Both are converted here so that the parsing stage only has to deal with plain
 * ASCII operators.</p>
 */
public final class ExpressionNormalizer {

    private static final Map<Character, String> REPLACEMENTS = new HashMap<Character, String>();

    static {
        REPLACEMENTS.put('×', "*");
        REPLACEMENTS.put('＊', "*");
        REPLACEMENTS.put('÷', "/");
        REPLACEMENTS.put('／', "/");
        REPLACEMENTS.put('－', "-");
        REPLACEMENTS.put('—', "-");
        REPLACEMENTS.put('–', "-");
        REPLACEMENTS.put('＋', "+");
        REPLACEMENTS.put('（', "(");
        REPLACEMENTS.put('）', ")");
    }

    private ExpressionNormalizer() {
    }

    /**
     * Returns the canonical form of {@code raw}: display symbols replaced, whitespace removed.
     *
     * @param raw raw user input, may be {@code null}
     * @return canonical expression, never {@code null}
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char current = raw.charAt(i);
            if (Character.isWhitespace(current)) {
                continue;
            }
            String replacement = REPLACEMENTS.get(current);
            if (replacement != null) {
                builder.append(replacement);
            } else {
                builder.append(current);
            }
        }
        return builder.toString();
    }
}
