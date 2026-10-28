package seedu.tuitionbook.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tuitionbook.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.tuitionbook.testutil.Assert.assertThrows;
import static seedu.tuitionbook.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.logic.commands.AddCommand;
import seedu.tuitionbook.logic.commands.ClearCommand;
import seedu.tuitionbook.logic.commands.DeleteCommand;
import seedu.tuitionbook.logic.commands.EditCommand;
import seedu.tuitionbook.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.tuitionbook.logic.commands.ExitCommand;
import seedu.tuitionbook.logic.commands.FindCommand;
import seedu.tuitionbook.logic.commands.HelpCommand;
import seedu.tuitionbook.logic.commands.LinkCommand;
import seedu.tuitionbook.logic.commands.ListCommand;
import seedu.tuitionbook.logic.commands.ReloadCommand;
import seedu.tuitionbook.logic.commands.TagCommand;
import seedu.tuitionbook.logic.commands.UnlinkCommand;
import seedu.tuitionbook.logic.commands.UntagCommand;
import seedu.tuitionbook.logic.commands.ViewCommand;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;
import seedu.tuitionbook.model.person.NameContainsKeywordsPredicate;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.PersonHasRolePredicate;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.model.tag.Tag;
import seedu.tuitionbook.testutil.EditPersonDescriptorBuilder;
import seedu.tuitionbook.testutil.PersonBuilder;
import seedu.tuitionbook.testutil.PersonUtil;

public class TuitionBookParserTest {

    private final TuitionBookParser parser = new TuitionBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " confirm") instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + "    confirm") instanceof ClearCommand);

        assertThrows(ParseException.class, ClearCommand.MESSAGE_CONFIRMATION_REQUIRED, () -> parser.parseCommand(
                ClearCommand.COMMAND_WORD));
        assertThrows(ParseException.class, ClearCommand.MESSAGE_INVALID_CONFIRMATION, () -> parser.parseCommand(
                ClearCommand.COMMAND_WORD + " 3"));
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertThrows(ParseException.class, () -> parser.parseCommand(ExitCommand.COMMAND_WORD + " 3"));
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(HelpCommand.COMMAND_WORD + " 3"));
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertEquals(new ListCommand(new PersonHasRolePredicate(Role.STUDENT)),
                parser.parseCommand(ListCommand.COMMAND_WORD + " r/student"));
        assertThrows(ParseException.class, () -> parser.parseCommand(ListCommand.COMMAND_WORD + " 3"));
    }

    @Test
    public void parseCommand_link() throws Exception {
        assertEquals(new LinkCommand(INDEX_FIRST_PERSON, seedu.tuitionbook.testutil.TypicalIndexes.INDEX_SECOND_PERSON),
                parser.parseCommand(LinkCommand.COMMAND_WORD + " 1 2"));
    }

    @Test
    public void parseCommand_tagAndUntag() throws Exception {
        assertEquals(new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("Math"))),
                parser.parseCommand("tag 1 t/Math"));
        assertEquals(new UntagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("Math"))),
                parser.parseCommand("untag 1 t/Math"));
    }

    @Test
    public void parseCommand_unlink() throws Exception {
        assertEquals(new UnlinkCommand(INDEX_FIRST_PERSON),
                parser.parseCommand(UnlinkCommand.COMMAND_WORD + " 1"));
    }

    @Test
    public void parseCommand_view() throws Exception {
        assertEquals(new ViewCommand(INDEX_FIRST_PERSON),
                parser.parseCommand(ViewCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased()));
        assertEquals(new ViewCommand(INDEX_FIRST_PERSON, true),
                parser.parseCommand(ViewCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased() + " v/"));
        assertThrows(ParseException.class, () -> parser.parseCommand(ViewCommand.COMMAND_WORD));
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }

    @Test
    public void parseCommand_noArgumentCommands_rejectExtraArgumentsInEveryCasing() throws Exception {
        for (String word : List.of("help", "exit", "reload")) {
            assertEquals(parser.parseCommand(word).getClass(),
                    parser.parseCommand(" \t" + word.toUpperCase(Locale.ROOT) + " \t ").getClass());
            for (String extra : List.of(" extra", " 1", " confirm", " n/Amy")) {
                assertThrows(ParseException.class, () -> parser.parseCommand(word + extra));
                assertThrows(ParseException.class, () -> parser.parseCommand(word.toUpperCase(Locale.ROOT) + extra));
            }
        }
        assertTrue(parser.parseCommand("reload") instanceof ReloadCommand);
    }

    @Test
    public void parseCommand_allCommandWords_acceptUpperAndMixedCase() throws Exception {
        for (String input : List.of("add r/student n/Amy p/123", "edit 1 n/Amy", "delete 1", "clear confirm",
                "find Amy", "list r/student", "link 1 2", "unlink 1", "view 1 v/", "help", "exit", "reload",
                "tag 1 t/Math", "untag 1 t/Math")) {
            int boundary = input.indexOf(' ');
            String word = boundary < 0 ? input : input.substring(0, boundary);
            String arguments = boundary < 0 ? "" : input.substring(boundary);
            Class<?> expected = parser.parseCommand(input).getClass();
            assertEquals(expected, parser.parseCommand(word.toUpperCase(Locale.ROOT) + arguments).getClass());
            String mixedWord = word.substring(0, 1) + word.substring(1).toUpperCase(Locale.ROOT);
            assertEquals(expected, parser.parseCommand(mixedWord + arguments).getClass());
        }
    }

    @Test
    public void parseCommand_commandCase_doesNotChangeValuesPrefixesOrConfirmation() throws Exception {
        Person expected = new PersonBuilder().withName("Rina a/p TAN").withPhone("123")
                .withoutEmail().withAddress("Blk\u00a012\u3000Road").withTags("Math", "call p/Next Week").build();
        assertEquals(new AddCommand(expected), parser.parseCommand(
                "AdD r/STUDENT n/\"Rina a/p TAN\" p/123 a/Blk\u00a012\u3000Road t/Math t/\"call p/Next Week\""));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(List.of("Rina", "TAN"))),
                parser.parseCommand("FiNd Rina TAN"));
        assertThrows(ParseException.class, () -> parser.parseCommand("ADD R/student n/Amy p/123"));
        assertThrows(ParseException.class, () -> parser.parseCommand("CLEAR CONFIRM"));
        assertThrows(ParseException.class, () -> parser.parseCommand("CLEAR"));
    }

    @Test
    public void parseCommand_commandCase_isIndependentOfDefaultLocale() throws Exception {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertTrue(parser.parseCommand("LIST") instanceof ListCommand);
            assertTrue(parser.parseCommand("LINK 1 2") instanceof LinkCommand);
            assertTrue(parser.parseCommand("UNLINK 1") instanceof UnlinkCommand);
            assertTrue(parser.parseCommand("EXIT") instanceof ExitCommand);
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void parseCommand_lineBreaks_reportedBeforeTrimmingOrDispatch() {
        // Scenarios adapted from https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/322
        String expectedMessage = "Commands must be entered on a single line. Remove the line break and try again.";
        for (char separator : new char[] {'\n', '\r', '\u000b', '\f', '\u0085', '\u2028', '\u2029'}) {
            for (String input : List.of(separator + "list", "list" + separator, "li" + separator + "st",
                    "list " + separator + "help", "add r/student n/Amy p/123 a/12" + separator + "Road",
                    "edit 1 n/\"Amy" + separator + " Tan\"")) {
                assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(input));
            }
        }
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("list\r\nhelp"));
    }

    @Test
    public void parseCommand_unicodeSyntaxSpaces_reportActionableDiagnostics() {
        // Scenarios adapted from https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/129
        // and https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/187
        // and https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/374
        // and https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/188
        String expectedMessage = "Your command contains an unusual space character, often from copied text. "
                + "Retype the spaces using the spacebar.";
        for (char space : new char[] {'\u00a0', '\u3000'}) {
            for (String input : List.of(space + "list", "list" + space, "LiSt" + space + "r/student",
                    "help " + space, "exit " + space, "reload " + space, "delete " + space + "1",
                    "edit 1" + space + " n/Amy", "tag 1" + space + " t/Math", "untag " + space + "1 t/Math",
                    "unlink 1" + space, "link 1" + space + "2", "link 1 " + space + "2",
                    "view 1" + space + " v/")) {
                assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(input));
            }
        }
    }
}
