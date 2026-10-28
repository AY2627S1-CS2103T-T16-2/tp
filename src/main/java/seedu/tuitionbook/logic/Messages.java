package seedu.tuitionbook.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.tuitionbook.logic.parser.Prefix;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.tag.Tag;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The contact index provided is invalid.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d person(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Each of these fields can only be given once: ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Returns an error message for an index outside the displayed person list.
     */
    public static String getInvalidPersonDisplayedIndexMessage(int displayedPersonCount) {
        if (displayedPersonCount == 0) {
            return MESSAGE_INVALID_PERSON_DISPLAYED_INDEX
                    + " No contacts are shown in the current list. Use list to show all contacts.";
        }

        String verb = displayedPersonCount == 1 ? "is" : "are";
        String noun = displayedPersonCount == 1 ? "contact" : "contacts";
        return String.format("%s There %s %d %s in the current list. Use an index from 1 to %d.",
                MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, verb, displayedPersonCount, noun, displayedPersonCount);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Role: ")
                .append(person.getRole().name())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail().map(Object::toString).orElse("-"))
                .append("; Address: ")
                .append(person.getAddress().map(Object::toString).orElse("-"))
                .append("; Tags: ");
        person.getTags().forEach(tag -> builder.append(formatTag(tag)));
        return builder.toString();
    }

    /**
     * Formats {@code tag} in brackets, escaping backslashes and brackets in its name so that adjacent tags
     * remain distinguishable from a single tag containing brackets.
     */
    public static String formatTag(Tag tag) {
        String escapedTagName = tag.tagName.replace("\\", "\\\\")
                .replace("[", "\\[")
                .replace("]", "\\]");
        return "[" + escapedTagName + "]";
    }

}
