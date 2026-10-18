package seedu.tuitionbook.logic.parser;

import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tuitionbook.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.tuitionbook.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Locale;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.logic.commands.ListCommand;
import seedu.tuitionbook.model.person.PersonHasRolePredicate;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.model.person.PersonMatchesListFilterPredicate;

public class ListCommandParserTest {

    private ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_emptyArgs_returnsListCommandForAllPersons() {
        assertParseSuccess(parser, "", new ListCommand());
        assertParseSuccess(parser, "   ", new ListCommand());
    }

    @Test
    public void parse_validRole_returnsFilteredListCommand() {
        ListCommand expectedStudentCommand = new ListCommand(new PersonHasRolePredicate(Role.STUDENT));
        assertParseSuccess(parser, " r/student", expectedStudentCommand);
        assertParseSuccess(parser, " r/ STUDENT ", expectedStudentCommand);

        ListCommand expectedGuardianCommand = new ListCommand(new PersonHasRolePredicate(Role.GUARDIAN));
        assertParseSuccess(parser, " r/guardian", expectedGuardianCommand);
    }

    @Test
    @ResourceLock(Resources.LOCALE)
    public void parse_rolesInTurkishLocale_success() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            ListCommand expectedGuardianCommand = new ListCommand(new PersonHasRolePredicate(Role.GUARDIAN));
            assertParseSuccess(parser, " r/guardian", expectedGuardianCommand);
            assertParseSuccess(parser, " r/GUARDIAN", expectedGuardianCommand);
            assertParseSuccess(parser, " r/GuArDiAn", expectedGuardianCommand);
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void parse_invalidRole_throwsParseException() {
        assertParseFailure(parser, " r/teacher", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " r/", Role.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_nonEmptyPreamble_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " 3", expectedMessage);
        assertParseFailure(parser, " students", expectedMessage);
        assertParseFailure(parser, " 3 r/student", expectedMessage);
    }

    @Test
    public void parse_duplicateRolePrefix_throwsParseException() {
        assertParseFailure(parser, " r/student r/guardian",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ROLE));
    }

    @Test
    public void parse_tagsAndRole_returnsCombinedFilter() {
        assertParseSuccess(parser, " r/student t/sec4 t/AMath",
                new ListCommand(new PersonMatchesListFilterPredicate(Role.STUDENT, java.util.List.of("sec4", "AMath"))));
    }

    @Test
    public void parse_invalidTag_throwsParseException() {
        assertParseFailure(parser, " t/not valid", seedu.tuitionbook.model.tag.Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_duplicateTagValues_returnsFilterWithBothTags() {
        ListCommand expected = new ListCommand(new PersonMatchesListFilterPredicate(
                null, List.of("Sec4", "sec4")));

        assertParseSuccess(parser, " t/Sec4 t/sec4", expected);
    }
}
