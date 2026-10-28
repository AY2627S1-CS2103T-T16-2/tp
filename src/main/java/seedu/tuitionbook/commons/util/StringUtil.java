package seedu.tuitionbook.commons.util;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.commons.util.AppUtil.checkArgument;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * Helper functions for handling strings.
 */
public class StringUtil {

    /**
     * Returns true if the {@code sentence} contains the {@code word}.
     *   Ignores case, but a full word match is required.
     *   <br>examples:<pre>
     *       containsWordIgnoreCase("ABc def", "abc") == true
     *       containsWordIgnoreCase("ABc def", "DEF") == true
     *       containsWordIgnoreCase("ABc def", "AB") == false //not a full word match
     *       </pre>
     * @param sentence cannot be null
     * @param word cannot be null, cannot be empty, must be a single word
     */
    public static boolean containsWordIgnoreCase(String sentence, String word) {
        requireNonNull(sentence);
        requireNonNull(word);

        String preppedWord = word.trim();
        checkArgument(!preppedWord.isEmpty(), "Word parameter cannot be empty");
        checkArgument(preppedWord.split("\\s+").length == 1, "Word parameter should be a single word");

        String preppedSentence = sentence;
        String[] wordsInPreppedSentence = preppedSentence.split("\\s+");

        return Arrays.stream(wordsInPreppedSentence)
                .anyMatch(preppedWord::equalsIgnoreCase);
    }

    /**
     * Returns a detailed message of {@code t}, including the stack trace.
     */
    public static String getDetails(Throwable t) {
        requireNonNull(t);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return t.getMessage() + "\n" + sw.toString();
    }

    /**
     * Returns true if {@code s} contains only ASCII characters (U+0000 to U+007F).
     *
     * @throws NullPointerException if {@code s} is null.
     */
    public static boolean isAscii(String s) {
        requireNonNull(s);
        return s.chars().allMatch(c -> c < 0x80);
    }

    /** Returns whether {@code character} is an ASCII space, tab, LF, vertical tab, form feed or CR. */
    public static boolean isAsciiWhitespace(char character) {
        return character == ' ' || (character >= '\t' && character <= '\r');
    }

    /**
     * Trims ASCII whitespace while preserving other controls and non-ASCII characters for validation.
     * Unlike {@link String#trim()}, this never discards characters such as NUL or escape.
     */
    public static String trimAsciiWhitespace(String s) {
        requireNonNull(s);
        int start = 0;
        int end = s.length();
        while (start < end && isAsciiWhitespace(s.charAt(start))) {
            start++;
        }
        while (end > start && isAsciiWhitespace(s.charAt(end - 1))) {
            end--;
        }
        return s.substring(start, end);
    }

    /**
     * Returns true if {@code s} represents a non-zero unsigned integer written with ASCII digits
     * e.g. 1, 2, 3, ..., {@code Integer.MAX_VALUE} <br>
     * Will return false for any other non-null string input
     * e.g. empty string, "-1", "0", "+1", and " 2 " (untrimmed), "3 0" (contains whitespace), "1 a" (contains letters),
     * and non-ASCII digits such as full-width or Arabic-Indic digits that {@link Integer#parseInt(String)} accepts.
     * @throws NullPointerException if {@code s} is null.
     */
    public static boolean isNonZeroUnsignedInteger(String s) {
        requireNonNull(s);
        if (s.isEmpty() || !s.chars().allMatch(c -> c >= '0' && c <= '9')) {
            return false;
        }

        try {
            return Integer.parseInt(s) > 0;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
