package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.tuitionbook.model.person.Address;
import seedu.tuitionbook.model.person.Name;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.PersonBuilder;

/**
 * JavaFX integration tests for the contact-card display.
 */
@EnabledIfEnvironmentVariable(named = "RUN_JAVAFX_TESTS", matches = "true")
public class CardDisplayUiTest {

    private static final int OVERSIZED_TAG_LENGTH = 1000;
    private static final double TAG_OVERFLOW_SCENE_WIDTH = 340;
    private static final double TAG_OVERFLOW_SCENE_HEIGHT = 500;
    private static final double TAG_RESIZED_WINDOW_WIDTH = 700;
    private static final double LAYOUT_BOUNDS_TOLERANCE = 0.5;

    @BeforeAll
    public static void startJavaFxToolkit() {
        JavaFxTestUtil.initialize();
    }

    @Test
    public void personCard_studentWithDetails_displaysLabeledValues() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();
            Person student = new PersonBuilder()
                    .withName("Sam Student")
                    .withPhone("98765432")
                    .withEmail("sam@example.com")
                    .withAddress("25 Clementi Road")
                    .withGuardian(guardian)
                    .withTags("secondary", "math")
                    .build();

            PersonCard card = new PersonCard(student, 1, guardian.getName().fullName);

            assertEquals("STUDENT", getLabel(card, "role").getText());
            assertTrue(getLabel(card, "role").getStyleClass().contains("student"));
            assertEquals("Phone: 98765432", getLabel(card, "phone").getText());
            assertEquals("Guardian: Grace Guardian", getLabel(card, "guardian").getText());
            assertTrue(getLabel(card, "guardian").isManaged());
            assertTrue(getLabel(card, "guardian").isVisible());
            assertEquals("Address: 25 Clementi Road", getLabel(card, "address").getText());
            assertEquals("Email: sam@example.com", getLabel(card, "email").getText());
            assertEquals(List.of("math", "secondary"), getTagNames(card));
            return null;
        });
    }

    @Test
    public void personCard_studentWithoutOptionalDetails_displaysPlaceholders() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            Person student = new PersonBuilder().withoutEmail().withoutAddress().build();

            PersonCard card = new PersonCard(student, 1, "-");

            assertEquals("Guardian: -", getLabel(card, "guardian").getText());
            assertEquals("Address: -", getLabel(card, "address").getText());
            assertEquals("Email: -", getLabel(card, "email").getText());
            assertEquals("Address: -", getLabel(card, "address").getTooltip().getText());
            assertEquals("Email: -", getLabel(card, "email").getTooltip().getText());
            return null;
        });
    }

    @Test
    public void personCard_guardian_hidesGuardianRow() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            Person guardian = new PersonBuilder().withRole(Role.GUARDIAN).build();

            PersonCard card = new PersonCard(guardian, 1, "-");

            assertEquals("GUARDIAN", getLabel(card, "role").getText());
            assertTrue(getLabel(card, "role").getStyleClass().contains("guardian"));
            assertFalse(getLabel(card, "guardian").isManaged());
            assertFalse(getLabel(card, "guardian").isVisible());
            return null;
        });
    }

    @Test
    public void personCard_guardianInTurkishLocale_usesLocaleIndependentCssClass() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            Locale originalLocale = Locale.getDefault();
            try {
                Locale.setDefault(Locale.forLanguageTag("tr"));
                Person guardian = new PersonBuilder().withRole(Role.GUARDIAN).build();

                PersonCard card = new PersonCard(guardian, 1, "-");

                assertTrue(getLabel(card, "role").getStyleClass().contains("guardian"));
            } finally {
                Locale.setDefault(originalLocale);
            }
            return null;
        });
    }

    @Test
    public void personCard_maximumLengthValues_keepFullValuesAccessibleAndRoleBadgeReadable() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            String maximumLengthName = "A".repeat(Name.MAX_LENGTH);
            String maximumLengthAddress = "X".repeat(Address.MAX_LENGTH);
            String longPhone = "9".repeat(200);
            String longEmail = "a".repeat(200) + "@example.com";
            Person guardian = new PersonBuilder().withName(maximumLengthName).withRole(Role.GUARDIAN)
                    .withPhone(longPhone).withEmail(longEmail).build();
            Person student = new PersonBuilder().withName(maximumLengthName).withPhone(longPhone)
                    .withEmail(longEmail).withAddress(maximumLengthAddress).withGuardian(guardian).build();
            PersonCard card = new PersonCard(student, 1, guardian.getName().fullName);
            PersonCard guardianCard = new PersonCard(guardian, 2, "-");
            Stage stage = new Stage();

            try {
                stage.setScene(new Scene(new VBox(card.getRoot(), guardianCard.getRoot()), 340, 400));
                stage.show();
                card.getRoot().applyCss();
                card.getRoot().layout();

                Label name = getLabel(card, "name");
                Label role = getLabel(card, "role");
                Label phone = getLabel(card, "phone");
                Label guardianName = getLabel(card, "guardian");
                Label address = getLabel(card, "address");
                Label email = getLabel(card, "email");
                assertEquals(maximumLengthName, name.getText());
                assertEquals("Phone: " + longPhone, phone.getText());
                assertEquals("Guardian: " + maximumLengthName, guardianName.getText());
                assertEquals("Address: " + maximumLengthAddress, address.getText());
                assertEquals("Email: " + longEmail, email.getText());
                assertEquals(maximumLengthName, name.getTooltip().getText());
                assertEquals("Phone: " + longPhone, phone.getTooltip().getText());
                assertEquals("Guardian: " + maximumLengthName, guardianName.getTooltip().getText());
                assertEquals("Address: " + maximumLengthAddress, address.getTooltip().getText());
                assertEquals("Email: " + longEmail, email.getTooltip().getText());
                assertTrue(name.getTooltip().isWrapText());
                assertTrue(phone.getTooltip().isWrapText());
                assertTrue(guardianName.getTooltip().isWrapText());
                assertTrue(address.getTooltip().isWrapText());
                assertTrue(email.getTooltip().isWrapText());
                assertTrue(name.getWidth() <= card.getRoot().getWidth());
                assertTrue(phone.getWidth() <= card.getRoot().getWidth());
                assertTrue(guardianName.getWidth() <= card.getRoot().getWidth());
                assertTrue(address.getWidth() <= card.getRoot().getWidth());
                assertTrue(email.getWidth() <= card.getRoot().getWidth());
                assertTrue(role.getWidth() >= role.prefWidth(-1));
                Label guardianPhone = getLabel(guardianCard, "phone");
                Label guardianEmail = getLabel(guardianCard, "email");
                Label guardianRole = getLabel(guardianCard, "role");
                assertTrue(guardianPhone.getWidth() <= guardianCard.getRoot().getWidth());
                assertTrue(guardianEmail.getWidth() <= guardianCard.getRoot().getWidth());
                assertTrue(guardianRole.getWidth() >= guardianRole.prefWidth(-1));
            } finally {
                stage.close();
            }
            return null;
        });
    }

    @Test
    public void personCard_tags_stayWithinCardAndRemainAccessibleAfterResize() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            String longTag = "x".repeat(OVERSIZED_TAG_LENGTH);
            Person longTagPerson = new PersonBuilder().withTags(longTag).build();
            Person multipleTagsPerson = new PersonBuilder().withTags("zulu", "alpha", "middle").build();
            Person noTagsPerson = new PersonBuilder().withTags().build();
            PersonCard longTagCard = new PersonCard(longTagPerson, 1, "-");
            PersonCard multipleTagsCard = new PersonCard(multipleTagsPerson, 2, "-");
            PersonCard noTagsCard = new PersonCard(noTagsPerson, 3, "-");
            VBox cards = new VBox(longTagCard.getRoot(), multipleTagsCard.getRoot(), noTagsCard.getRoot());
            Stage stage = new Stage();

            try {
                stage.setScene(new Scene(cards, TAG_OVERFLOW_SCENE_WIDTH, TAG_OVERFLOW_SCENE_HEIGHT));
                stage.show();
                layout(cards);

                Label longTagLabel = getTagLabels(longTagCard).get(0);
                assertEquals(longTag, longTagLabel.getText());
                assertEquals(longTag, longTagLabel.getTooltip().getText());
                assertTrue(longTagLabel.getTooltip().isWrapText());
                assertEquals(List.of("alpha", "middle", "zulu"), getTagNames(multipleTagsCard));
                assertTrue(getTagLabels(noTagsCard).isEmpty());
                assertTagsStayInsideCard(longTagCard);
                assertTagsStayInsideCard(multipleTagsCard);

                stage.setWidth(TAG_RESIZED_WINDOW_WIDTH);
                layout(cards);
                assertTagsStayInsideCard(longTagCard);
                assertTagsStayInsideCard(multipleTagsCard);
                assertTrue(getLabel(longTagCard, "role").getWidth()
                        >= getLabel(longTagCard, "role").prefWidth(-1));
            } finally {
                stage.close();
            }
            return null;
        });
    }

    @Test
    public void personListPanel_guardianFilteredOut_displaysGuardianName() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();
            Person student = new PersonBuilder().withName("Sam Student").withGuardian(guardian).build();
            ObservableList<Person> filteredPersons = FXCollections.observableArrayList(student);
            ObservableList<Person> allPersons = FXCollections.observableArrayList(student, guardian);

            PersonListPanel panel = new PersonListPanel(filteredPersons, allPersons);
            ListView<Person> personListView = getPersonListView(panel);
            ListCell<Person> studentCell = createCell(personListView, 0);

            assertSame(filteredPersons, personListView.getItems());
            assertEquals("Guardian: Grace Guardian", getLabel(studentCell, "guardian").getText());
            return null;
        });
    }

    @Test
    public void personListPanel_allPersonsChanges_refreshGuardianName() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();
            Person student = new PersonBuilder().withName("Sam Student").withGuardian(guardian).build();
            ObservableList<Person> filteredPersons = FXCollections.observableArrayList(student);
            ObservableList<Person> allPersons = FXCollections.observableArrayList(student);
            PersonListPanel panel = new PersonListPanel(filteredPersons, allPersons);
            ListView<Person> personListView = getPersonListView(panel);
            Stage stage = new Stage();

            try {
                stage.setScene(new Scene(panel.getRoot(), 400, 300));
                stage.show();
                ListCell<Person> displayedStudentCell = getDisplayedCell(personListView, student);

                assertEquals("Guardian: -", getLabel(displayedStudentCell, "guardian").getText());

                allPersons.add(guardian);
                displayedStudentCell = getDisplayedCell(personListView, student);

                assertEquals("Guardian: Grace Guardian",
                        getLabel(displayedStudentCell, "guardian").getText());

                Person renamedGuardian = new PersonBuilder(guardian).withName("Grace Lim").build();
                allPersons.set(1, renamedGuardian);
                displayedStudentCell = getDisplayedCell(personListView, student);

                assertEquals("Guardian: Grace Lim", getLabel(displayedStudentCell, "guardian").getText());

                allPersons.remove(renamedGuardian);
                displayedStudentCell = getDisplayedCell(personListView, student);

                assertEquals("Guardian: -", getLabel(displayedStudentCell, "guardian").getText());
            } finally {
                stage.close();
            }
            return null;
        });
    }

    private static List<String> getTagNames(PersonCard card) {
        return getTagLabels(card).stream()
                .map(Label::getText)
                .toList();
    }

    private static List<Label> getTagLabels(PersonCard card) {
        FlowPane tags = (FlowPane) card.getRoot().lookup("#tags");
        assertNotNull(tags);
        return tags.getChildren().stream()
                .map(Label.class::cast)
                .toList();
    }

    private static void layout(VBox cards) {
        cards.applyCss();
        cards.layout();
    }

    private static void assertTagsStayInsideCard(PersonCard card) {
        FlowPane tags = (FlowPane) card.getRoot().lookup("#tags");
        Bounds cardBounds = card.getRoot().localToScene(card.getRoot().getBoundsInLocal());
        Bounds tagPaneBounds = tags.localToScene(tags.getBoundsInLocal());
        assertTrue(tagPaneBounds.getMinX() >= cardBounds.getMinX() - LAYOUT_BOUNDS_TOLERANCE);
        assertTrue(tagPaneBounds.getMaxX() <= cardBounds.getMaxX() + LAYOUT_BOUNDS_TOLERANCE,
                "Tag pane extends past card: " + tagPaneBounds + " vs " + cardBounds);
        for (Label tagLabel : getTagLabels(card)) {
            Bounds labelBounds = tagLabel.localToScene(tagLabel.getBoundsInLocal());
            assertTrue(labelBounds.getMaxX() <= cardBounds.getMaxX() + LAYOUT_BOUNDS_TOLERANCE,
                    "Tag extends past card: " + labelBounds + " vs " + cardBounds);
        }
    }

    private static Label getLabel(PersonCard card, String id) {
        Label label = (Label) card.getRoot().lookup("#" + id);
        assertNotNull(label);
        return label;
    }

    private static Label getLabel(ListCell<Person> cell, String id) {
        Label label = (Label) cell.getGraphic().lookup("#" + id);
        assertNotNull(label);
        return label;
    }

    @SuppressWarnings("unchecked")
    private static ListView<Person> getPersonListView(PersonListPanel panel) {
        ListView<Person> personListView = (ListView<Person>) panel.getRoot().lookup("#personListView");
        assertNotNull(personListView);
        return personListView;
    }

    private static ListCell<Person> createCell(ListView<Person> personListView, int index) {
        ListCell<Person> cell = personListView.getCellFactory().call(personListView);
        cell.updateListView(personListView);
        cell.updateIndex(index);
        assertNotNull(cell.getGraphic());
        return cell;
    }

    @SuppressWarnings("unchecked")
    private static ListCell<Person> getDisplayedCell(ListView<Person> personListView, Person person) {
        personListView.applyCss();
        personListView.layout();
        return personListView.lookupAll(".list-cell").stream()
                .filter(ListCell.class::isInstance)
                .map(node -> (ListCell<Person>) node)
                .filter(cell -> person.equals(cell.getItem()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Person does not have a displayed list cell"));
    }
}
