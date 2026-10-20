package seedu.tuitionbook.logic.parser;

import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import seedu.tuitionbook.logic.commands.ListCommand;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;
import seedu.tuitionbook.model.person.PersonHasRolePredicate;
import seedu.tuitionbook.model.person.PersonMatchesListFilterPredicate;
import seedu.tuitionbook.model.person.Role;

/**
 * Parses input arguments and creates a new ListCommand object
 */
public class ListCommandParser implements Parser<ListCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the ListCommand
     * and returns a ListCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public ListCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenizeWithQuotedValues(args, Set.of(PREFIX_TAG), PREFIX_ROLE);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_ROLE);

        Role role = null;
        if (argMultimap.getValue(PREFIX_ROLE).isPresent()) {
            role = ParserUtil.parseRole(argMultimap.getValue(PREFIX_ROLE).get());
        }

        if (argMultimap.getAllValues(PREFIX_TAG).isEmpty()) {
            return role == null ? new ListCommand() : new ListCommand(new PersonHasRolePredicate(role));
        }

        List<String> tagNames = new ArrayList<>();
        for (String tagValue : argMultimap.getAllValues(PREFIX_TAG)) {
            tagNames.add(ParserUtil.parseTag(tagValue).tagName);
        }
        return new ListCommand(new PersonMatchesListFilterPredicate(role, tagNames));
    }
}
