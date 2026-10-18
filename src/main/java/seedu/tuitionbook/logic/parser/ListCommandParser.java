package seedu.tuitionbook.logic.parser;

import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.stream.Collectors;

import seedu.tuitionbook.logic.commands.ListCommand;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;
import seedu.tuitionbook.model.person.PersonMatchesListFilterPredicate;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.model.person.PersonHasRolePredicate;

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
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_ROLE, PREFIX_TAG);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_ROLE);

        if (argMultimap.getValue(PREFIX_ROLE).isEmpty() && argMultimap.getAllValues(PREFIX_TAG).isEmpty()) {
            return new ListCommand();
        }

        Role role = argMultimap.getValue(PREFIX_ROLE).map(value -> {
            return ParserUtil.parseRole(value);
        }).orElse(null);
        if (argMultimap.getAllValues(PREFIX_TAG).isEmpty()) {
            assert role != null;
            return new ListCommand(new PersonHasRolePredicate(role));
        }
        return new ListCommand(new PersonMatchesListFilterPredicate(role,
                argMultimap.getAllValues(PREFIX_TAG).stream()
                        .map(value -> {
                            return ParserUtil.parseTag(value).tagName;
                        }).collect(Collectors.toList())));
    }
}
