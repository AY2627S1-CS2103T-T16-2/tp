package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ROLE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.ROLE_DESC_GUARDIAN;
import static seedu.address.logic.commands.CommandTestUtil.ROLE_DESC_STUDENT;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private final AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).withRole(Role.STUDENT).withTags(VALID_TAG_FRIEND).build();

        // Whitespace-only preamble.
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB
                + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + TAG_DESC_FRIEND, new AddCommand(expectedPerson));

        // Multiple tags are accepted.
        Person expectedPersonMultipleTags = new PersonBuilder(BOB).withRole(Role.STUDENT)
                .withTags(VALID_TAG_FRIEND, VALID_TAG_HUSBAND).build();
        assertParseSuccess(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                new AddCommand(expectedPersonMultipleTags));
    }

    @Test
    public void parse_studentRole_success() {
        Person expectedPerson = new PersonBuilder(BOB).withRole(Role.STUDENT).withTags().build();
        assertParseSuccess(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_guardianRole_success() {
        Person expectedPerson = new PersonBuilder(BOB).withRole(Role.GUARDIAN).withTags().build();
        assertParseSuccess(parser, ROLE_DESC_GUARDIAN + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_mixedCaseRole_success() {
        Person expectedPerson = new PersonBuilder(BOB).withRole(Role.GUARDIAN).withTags().build();
        assertParseSuccess(parser, " r/GuArDiAn" + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_emailOmitted_success() {
        Person expectedPerson = new PersonBuilder(BOB).withRole(Role.STUDENT).withoutEmail().withTags().build();
        assertParseSuccess(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_addressOmitted_success() {
        Person expectedPerson = new PersonBuilder(BOB).withRole(Role.STUDENT).withoutAddress().withTags().build();
        assertParseSuccess(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_emailAndAddressOmitted_success() {
        Person expectedPerson = new PersonBuilder(BOB).withRole(Role.STUDENT).withoutEmail().withoutAddress()
                .withTags().build();
        assertParseSuccess(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_repeatedNonTagValue_failure() {
        String validPerson = ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB;

        assertParseFailure(parser, ROLE_DESC_GUARDIAN + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ROLE));
        assertParseFailure(parser, NAME_DESC_AMY + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, PHONE_DESC_AMY + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        assertParseFailure(parser, EMAIL_DESC_AMY + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));
        assertParseFailure(parser, ADDRESS_DESC_AMY + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB, expectedMessage);
        assertParseFailure(parser, ROLE_DESC_STUDENT + VALID_NAME_BOB + PHONE_DESC_BOB, expectedMessage);
        assertParseFailure(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + VALID_PHONE_BOB, expectedMessage);
        assertParseFailure(parser, VALID_NAME_BOB + VALID_PHONE_BOB, expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        String mandatoryFields = ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB;

        assertParseFailure(parser, INVALID_ROLE_DESC + NAME_DESC_BOB + PHONE_DESC_BOB, Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, ROLE_DESC_STUDENT + INVALID_NAME_DESC + PHONE_DESC_BOB, Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, mandatoryFields + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, mandatoryFields + INVALID_ADDRESS_DESC, Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, mandatoryFields + INVALID_TAG_DESC, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + mandatoryFields,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_guardianPrefix_failure() {
        assertParseFailure(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB + " g/1",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }
}
