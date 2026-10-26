package seedu.tuitionbook.ui;

import javafx.scene.control.TextField;

/**
 * A command input field that preserves word boundaries in pasted text.
 *
 * <p>JavaFX removes some control characters before the command box can observe them. Replacing
 * supported separators before delegating to {@link TextField#replaceText(int, int, String)} keeps
 * pasted commands on one line without silently joining adjacent words.</p>
 */
public final class CommandTextField extends TextField {

    /**
     * Replaces the selected text while converting pasted separators to spaces.
     *
     * @param start the start position of the replacement
     * @param end the end position of the replacement
     * @param text the replacement text
     */
    @Override
    public void replaceText(int start, int end, String text) {
        super.replaceText(start, end, normalizeSeparators(text));
    }

    /**
     * Converts command separators to spaces, treating CRLF as one separator.
     */
    private static String normalizeSeparators(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder normalizedText = new StringBuilder(text.length());
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '\r') {
                normalizedText.append(' ');
                if (index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                    index++;
                }
            } else if (isSeparator(character)) {
                normalizedText.append(' ');
            } else {
                normalizedText.append(character);
            }
        }
        return normalizedText.toString();
    }

    /**
     * Returns whether the character is a non-carriage-return command separator.
     */
    private static boolean isSeparator(char character) {
        return character == '\n'
                || character == '\t'
                || character == '\u000B'
                || character == '\u000C'
                || character == '\u0085'
                || character == '\u2028'
                || character == '\u2029';
    }
}
