package net.oldust.core.scoreboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.text.FieldPosition;
import java.text.MessageFormat;

/**
 * An instance of this class will
 * symbolize a line of a scoreboard.
 * <p>
 * It contains a String with placeholders that will
 * be replaced and used in such scoreboard.
 */

@RequiredArgsConstructor
public class Line {
    private final MessageFormat messageFormat;

    @Getter
    private final String placeholdedLine;
    @Getter
    private final String baseLine;

    /**
     * Formats the first placeholder of the {@link #placeholdedLine}.
     * <p>
     * Example: given a placeholdedLine "Hello {0}, your money is {1}"
     * AND a parameter "Bob"
     * this method will modify {0} with "Bob", and let {1} unmodified.
     *
     * @param replacement replacement
     * @return the string with the replacements applied
     */

    public String format(Object replacement) {
        StringBuffer buffer = new StringBuffer();

        if (!(replacement instanceof Object[])) { // https://bugs.openjdk.java.net/browse/JDK-4228682
            replacement = new Object[]{replacement};
        }

        return messageFormat.format(replacement, buffer, null).toString();
    }

    /**
     * Formats a bunch of placeholders.
     * <p>
     * Example: given a placeholdedLine "Hello {0}, your money is {1}"
     * AND replacements "Bob" and "50$"
     * this method will modify {0} and {1} with "Bob" and "50$" respectively.
     *
     * @param replacements objects to replace in the placeholders
     * @return the string with the replacements applied
     */

    public String formatBunch(Object... replacements) {
        return format(replacements);
    }

    /**
     * Creates an instance of {@link Line}.
     *
     * @param text     the text with placeholders, example:
     *                 'Hello {0}, your money is {1}'. Placeholders MUST
     *                 be that way. No {}, no %s, no %, no <>.
     * @param replaces an initial replace for the line to be shown in the first time
     *                 it will be used just at the moment of appending the line,
     *                 given the prior example, the replaces would be: 'Bob' and '50$'
     * @return an instance of Line
     * @throws IllegalArgumentException if the pattern ({@param text}) is invalid
     */

    public static Line of(String text, Object... replaces) throws IllegalArgumentException {
        StringBuffer buffer = new StringBuffer();

        MessageFormat format = new MessageFormat(text);
        format.format(replaces, buffer, new FieldPosition(0));

        return new Line(format, text, buffer.toString());
    }

}
