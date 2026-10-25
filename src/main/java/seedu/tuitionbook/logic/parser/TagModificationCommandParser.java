package seedu.tuitionbook.logic.parser;

import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Set;
import java.util.function.BiFunction;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.logic.commands.Command;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;
import seedu.tuitionbook.model.tag.Tag;

/** Common argument parsing for the incremental tag commands. */
final class TagModificationCommandParser {
    private TagModificationCommandParser() {
    }

    /**
     * Parses a displayed contact index and one or more tags into an incremental tag command.
     *
     * @param <T> Type of command to construct.
     * @param args Arguments containing the contact index and prefixed tag values.
     * @param usage Command usage included in invalid-format errors.
     * @param factory Creates the command from the validated index and tag set.
     * @return Command containing the validated arguments.
     * @throws ParseException If the index, command format, or any tag is invalid.
     */
    static <T extends Command> T parse(String args, String usage, BiFunction<Index, Set<Tag>, T> factory)
            throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeWithQuotedValues(args, Set.of(PREFIX_TAG));
        ParserUtil.checkForUnsupportedSpaces(arguments.getPreamble());
        Index index;
        try {
            index = ParserUtil.parseIndex(arguments.getPreamble());
        } catch (ParseException exception) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage), exception);
        }
        if (arguments.getAllValues(PREFIX_TAG).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
        }
        Set<Tag> tags = ParserUtil.parseTags(arguments.getAllValues(PREFIX_TAG));
        return factory.apply(index, tags);
    }
}
