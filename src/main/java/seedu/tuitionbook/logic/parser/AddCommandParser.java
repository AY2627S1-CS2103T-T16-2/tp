package seedu.tuitionbook.logic.parser;

import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_GUARDIAN;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import seedu.tuitionbook.logic.commands.AddCommand;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;
import seedu.tuitionbook.model.person.Address;
import seedu.tuitionbook.model.person.Email;
import seedu.tuitionbook.model.person.Name;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Phone;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.model.tag.Tag;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenizeWithQuotedValues(args, Set.of(PREFIX_NAME, PREFIX_ADDRESS, PREFIX_TAG),
                        PREFIX_ROLE, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_GUARDIAN);

        if (argMultimap.getValue(PREFIX_GUARDIAN).isPresent()) {
            throw new ParseException(AddCommand.MESSAGE_GUARDIAN_PREFIX_NOT_ALLOWED);
        }

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_ROLE, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS);
        List<String> missingRequiredFields = getMissingRequiredFields(argMultimap);
        if (!missingRequiredFields.isEmpty()) {
            throw new ParseException(String.format(AddCommand.MESSAGE_MISSING_REQUIRED_FIELDS,
                    String.join(", ", missingRequiredFields), AddCommand.MESSAGE_USAGE));
        }

        Role role = ParserUtil.parseRole(argMultimap.getValue(PREFIX_ROLE).get());
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Phone phone = ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get());
        Email email = argMultimap.getValue(PREFIX_EMAIL).isPresent()
                ? ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get()) : null;
        Address address = argMultimap.getValue(PREFIX_ADDRESS).isPresent()
                ? ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get()) : null;
        Set<Tag> tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));

        Person person = new Person(role, name, phone, email, address, tagList);

        return new AddCommand(person);
    }

    /**
     * Returns the labels of missing mandatory fields in command-usage order.
     */
    private static List<String> getMissingRequiredFields(ArgumentMultimap argumentMultimap) {
        List<String> missingFields = new ArrayList<>();
        addIfMissing(argumentMultimap, PREFIX_ROLE, "ROLE", missingFields);
        addIfMissing(argumentMultimap, PREFIX_NAME, "NAME", missingFields);
        addIfMissing(argumentMultimap, PREFIX_PHONE, "PHONE", missingFields);
        return missingFields;
    }

    private static void addIfMissing(ArgumentMultimap argumentMultimap, Prefix prefix, String fieldName,
            List<String> missingFields) {
        if (argumentMultimap.getValue(prefix).isEmpty()) {
            missingFields.add(prefix + fieldName);
        }
    }

}
