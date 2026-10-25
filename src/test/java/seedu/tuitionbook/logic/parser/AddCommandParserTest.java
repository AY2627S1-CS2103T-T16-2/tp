package seedu.tuitionbook.logic.parser;

import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.EMPTY_ADDRESS_DESC;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.EMPTY_EMAIL_DESC;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.INVALID_ROLE_DESC;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.ROLE_DESC_GUARDIAN;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.ROLE_DESC_STUDENT;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.tuitionbook.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.tuitionbook.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.tuitionbook.testutil.TypicalPersons.BOB;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.logic.commands.AddCommand;
import seedu.tuitionbook.model.person.Address;
import seedu.tuitionbook.model.person.Email;
import seedu.tuitionbook.model.person.Name;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Phone;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.model.tag.Tag;
import seedu.tuitionbook.testutil.PersonBuilder;

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
    public void parse_hyphenatedTag_success() {
        String hyphenatedTag = "referred-by-Mrs-Lim";
        Person expectedPerson = new PersonBuilder(BOB).withRole(Role.STUDENT).withoutEmail().withoutAddress()
                .withTags(hyphenatedTag).build();

        assertParseSuccess(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB
                + " " + PREFIX_TAG + hyphenatedTag, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_flexibleTagsAndQuotedName_preservesPrefixBoundaries() {
        Person expected = new PersonBuilder().withRole(Role.STUDENT).withName("Rina a/p Kaur")
                .withPhone("91234567").withoutEmail().withAddress("123 Main Street")
                .withTags("Math/Science", "paid p.m.").build();
        assertParseSuccess(parser,
                " r/student n/\"Rina a/p Kaur\" p/91234567 t/Math/Science t/paid p.m. a/123 Main Street",
                new AddCommand(expected));
    }

    @Test
    public void parse_longMultiSegmentAndRepeatedTags_success() {
        String longMultiSegmentTag = "A-".repeat(5000) + "B";
        String secondTag = "Sec4";
        Person expectedPerson = new PersonBuilder(BOB).withRole(Role.STUDENT).withoutEmail().withoutAddress()
                .withTags(longMultiSegmentTag, secondTag).build();

        assertParseSuccess(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB
                + " " + PREFIX_TAG + longMultiSegmentTag + " " + PREFIX_TAG + secondTag,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_tagWithAddressPrefix_quotedTagProtectsPrefix() {
        String mandatoryFields = ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB;
        Person expected = new PersonBuilder(BOB).withRole(Role.STUDENT).withoutEmail()
                .withAddress("c room").withTags("meet at").build();
        assertParseSuccess(parser, mandatoryFields + " t/meet at a/c room", new AddCommand(expected));
        Person quoted = new PersonBuilder(BOB).withRole(Role.STUDENT).withoutEmail().withoutAddress()
                .withTags("meet at a/c room").build();
        assertParseSuccess(parser, mandatoryFields + " t/\"meet at a/c room\"", new AddCommand(quoted));
    }

    @Test
    public void parse_tagWithPhonePrefix_quotedTagProtectsPrefix() {
        assertParseFailure(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB + " t/call p/92345678",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        Person expected = new PersonBuilder(BOB).withRole(Role.STUDENT).withoutEmail().withoutAddress()
                .withTags("call p/92345678").build();
        assertParseSuccess(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB
                + " t/\"call p/92345678\"", new AddCommand(expected));
    }

    @Test
    public void parse_tagWithQuotes_removesEnclosingQuotes() {
        Person expected = new PersonBuilder(BOB).withRole(Role.STUDENT).withoutEmail().withoutAddress()
                .withTags("Sec 4").build();
        assertParseSuccess(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB + " t/\"Sec 4\"",
                new AddCommand(expected));
    }

    @Test
    public void parse_tagsWithEnclosingAndLiteralQuotes_preservesOnlyLiteralQuotes() {
        Person expected = new PersonBuilder(BOB).withRole(Role.STUDENT).withoutEmail().withoutAddress()
                .withTags("Sec 4", "Math \"advanced\" class", "Science\"").build();

        assertParseSuccess(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB
                + " t/\"Sec 4\" t/Math \"advanced\" class t/Science\"", new AddCommand(expected));
    }

    @Test
    public void parse_nonAsciiTag_reportsTagConstraints() {
        for (String tag : new String[] {"caf\u00e9", "\u6570\u5b66", "\ud83d\ude00", "Sec\u00a04"}) {
            assertParseFailure(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB + " t/" + tag,
                    Tag.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_internalTabTag_failure() {
        String mandatoryFields = ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB;

        assertParseFailure(parser, mandatoryFields + " " + PREFIX_TAG + "Ma\tth", Tag.MESSAGE_CONSTRAINTS);
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
    @ResourceLock(Resources.LOCALE)
    public void parse_rolesInTurkishLocale_success() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Person expectedGuardian = new PersonBuilder(BOB).withRole(Role.GUARDIAN).withoutEmail().withoutAddress()
                    .withTags().build();
            assertParseSuccess(parser, " r/guardian" + NAME_DESC_BOB + PHONE_DESC_BOB,
                    new AddCommand(expectedGuardian));
            assertParseSuccess(parser, " r/GUARDIAN" + NAME_DESC_BOB + PHONE_DESC_BOB,
                    new AddCommand(expectedGuardian));
            assertParseSuccess(parser, " r/GuArDiAn" + NAME_DESC_BOB + PHONE_DESC_BOB,
                    new AddCommand(expectedGuardian));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void parse_quotedNameWithForwardSlashParticle_success() {
        Person expectedPerson = new PersonBuilder().withName("Muhammad s/o Ali").withPhone("81234567")
                .withRole(Role.STUDENT).withoutEmail().withoutAddress().withTags().build();
        assertParseSuccess(parser, ROLE_DESC_STUDENT + " n/\"Muhammad s/o Ali\" p/81234567",
                new AddCommand(expectedPerson));
        assertParseFailure(parser, ROLE_DESC_STUDENT + " n/Muhammad s/o Ali p/81234567",
                String.format(ArgumentTokenizer.MESSAGE_UNKNOWN_PREFIX, "s/"));
    }

    @Test
    public void parse_quotedAddress_followsNormalFieldValidation() {
        String required = ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB;
        assertParseFailure(parser, required + " a/\"\"", Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, required + " a/\"   \"", Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, required + " a/\"" + "X".repeat(Address.MAX_LENGTH + 1) + "\"",
                Address.MESSAGE_CONSTRAINTS);
        Person expected = new PersonBuilder(BOB).withRole(Role.STUDENT).withoutEmail()
                .withAddress("X".repeat(Address.MAX_LENGTH)).withTags().build();
        assertParseSuccess(parser, required + " a/\"" + "X".repeat(Address.MAX_LENGTH) + "\"",
                new AddCommand(expected));
        Person literalQuotes = new PersonBuilder(expected).withAddress("\"Block A\"").build();
        assertParseSuccess(parser, required + " a/\"\\\"Block A\\\"\"", new AddCommand(literalQuotes));
    }

    @Test
    public void parse_widenedNameAndFormattedPhone_success() {
        String widenedName = "Mary-Jane O'Connor";
        String formattedPhone = "+65 9123-4567";
        Person expectedPerson = new PersonBuilder(BOB).withRole(Role.STUDENT).withName(widenedName)
                .withPhone(formattedPhone).withoutEmail().withoutAddress().withTags().build();

        assertParseSuccess(parser, ROLE_DESC_STUDENT + " " + PREFIX_NAME + widenedName
                + " " + PREFIX_PHONE + formattedPhone, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_maximumLengthNameAndAddress_success() {
        String maximumLengthName = "\uD801\uDC00".repeat(Name.MAX_LENGTH);
        String maximumLengthAddress = "\uD83D\uDE00".repeat(Address.MAX_LENGTH);
        Person expectedPerson = new PersonBuilder(BOB).withRole(Role.STUDENT).withName(maximumLengthName)
                .withPhone(VALID_PHONE_BOB).withoutEmail().withAddress(maximumLengthAddress).withTags().build();

        assertParseSuccess(parser, ROLE_DESC_STUDENT + " " + PREFIX_NAME + "  " + maximumLengthName
                + "  " + PREFIX_PHONE + VALID_PHONE_BOB + " " + PREFIX_ADDRESS + "  " + maximumLengthAddress + "  ",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_maximumLengthAsciiNameAndAddress_success() {
        String maximumLengthName = "A".repeat(Name.MAX_LENGTH);
        String maximumLengthAddress = "X".repeat(Address.MAX_LENGTH);
        Person expectedPerson = new PersonBuilder(BOB).withRole(Role.STUDENT).withName(maximumLengthName)
                .withPhone(VALID_PHONE_BOB).withoutEmail().withAddress(maximumLengthAddress).withTags().build();

        assertParseSuccess(parser, ROLE_DESC_STUDENT + " " + PREFIX_NAME + maximumLengthName
                + " " + PREFIX_PHONE + VALID_PHONE_BOB + " " + PREFIX_ADDRESS + maximumLengthAddress,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_slashContainingNameAtLengthBoundary_enforcesMaximum() {
        String maximumLengthName = "A/" + "B".repeat(Name.MAX_LENGTH - 2);
        String overLengthName = maximumLengthName + "B";
        Person expectedPerson = new PersonBuilder(BOB).withRole(Role.STUDENT).withName(maximumLengthName)
                .withPhone(VALID_PHONE_BOB).withoutEmail().withoutAddress().withTags().build();

        assertParseSuccess(parser, ROLE_DESC_STUDENT + " " + PREFIX_NAME + maximumLengthName + PHONE_DESC_BOB,
                new AddCommand(expectedPerson));
        assertParseFailure(parser, ROLE_DESC_STUDENT + " " + PREFIX_NAME + overLengthName + PHONE_DESC_BOB,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_overLengthNameAndAddress_failure() {
        String mandatoryFields = ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB;
        String overLengthName = "A".repeat(Name.MAX_LENGTH + 1);
        String overLengthAddress = "A".repeat(Address.MAX_LENGTH + 1);

        assertParseFailure(parser, ROLE_DESC_STUDENT + " " + PREFIX_NAME + overLengthName + PHONE_DESC_BOB,
                Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, mandatoryFields + " " + PREFIX_ADDRESS + overLengthAddress,
                Address.MESSAGE_CONSTRAINTS);
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

        // Repeated fields with valid values.
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

        // Invalid value followed by valid value.
        assertParseFailure(parser, INVALID_ROLE_DESC + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ROLE));
        assertParseFailure(parser, INVALID_NAME_DESC + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, INVALID_PHONE_DESC + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        assertParseFailure(parser, INVALID_EMAIL_DESC + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));
        assertParseFailure(parser, EMPTY_ADDRESS_DESC + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // Valid value followed by invalid value.
        assertParseFailure(parser, validPerson + INVALID_ROLE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ROLE));
        assertParseFailure(parser, validPerson + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, validPerson + INVALID_PHONE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        assertParseFailure(parser, validPerson + INVALID_EMAIL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));
        assertParseFailure(parser, validPerson + EMPTY_ADDRESS_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // Every duplicated single-valued prefix is identified in the combined error.
        assertParseFailure(parser, validPerson + ROLE_DESC_GUARDIAN + NAME_DESC_AMY + PHONE_DESC_AMY
                        + EMAIL_DESC_AMY + ADDRESS_DESC_AMY,
                Messages.getErrorMessageForDuplicatePrefixes(
                        PREFIX_ROLE, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS));
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
        // An empty e/ is rejected; omit e/ entirely to add a contact without an email.
        assertParseFailure(parser, mandatoryFields + EMPTY_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, mandatoryFields + EMPTY_ADDRESS_DESC, Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, mandatoryFields + INVALID_TAG_DESC, Tag.MESSAGE_CONSTRAINTS);

        // When two values are invalid, the first parsed error is reported.
        assertParseFailure(parser, ROLE_DESC_STUDENT + INVALID_NAME_DESC + PHONE_DESC_BOB + INVALID_EMAIL_DESC,
                Name.MESSAGE_CONSTRAINTS);

        assertParseFailure(parser, PREAMBLE_NON_EMPTY + mandatoryFields,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_guardianPrefix_failure() {
        assertParseFailure(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB + " g/1",
                AddCommand.MESSAGE_GUARDIAN_PREFIX_NOT_ALLOWED);
        assertParseFailure(parser, ROLE_DESC_STUDENT + NAME_DESC_BOB + PHONE_DESC_BOB + " g/",
                AddCommand.MESSAGE_GUARDIAN_PREFIX_NOT_ALLOWED);
    }
}
