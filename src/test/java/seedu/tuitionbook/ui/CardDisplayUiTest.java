package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import javafx.scene.control.OverrunStyle;
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
            Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("91234567")
                    .withRole(Role.GUARDIAN).build();
            Person student = new PersonBuilder()
                    .withName("Sam Student")
                    .withPhone("98765432")
                    .withEmail("sam@example.com")
                    .withAddress("25 Clementi Road")
                    .withGuardian(guardian)
                    .withTags("secondary", "math")
                    .build();

            PersonCard card = new PersonCard(student, 1, "Grace Guardian (91234567)");

            assertEquals("STUDENT", getLabel(card, "role").getText());
            assertTrue(getLabel(card, "role").getStyleClass().contains("student"));
            assertEquals("Phone: 98765432", getLabel(card, "phone").getText());
            assertEquals("Guardian: Grace Guardian (91234567)", getLabel(card, "relationship").getText());
            assertTrue(getLabel(card, "relationship").isManaged());
            assertTrue(getLabel(card, "relationship").isVisible());
            assertEquals("Address: 25 Clementi Road", getLabel(card, "address").getText());
            assertEquals("Email: sam@example.com", getLabel(card, "email").getText());
            assertEquals(List.of("math", "secondary"), getTagNames(card));
            return null;
        });
    }

    @Test
    public void personCard_tags_useLocaleIndependentCaseInsensitiveOrder() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            Locale originalLocale = Locale.getDefault();
            try {
                for (Locale locale : List.of(Locale.ROOT, Locale.ENGLISH,
                        Locale.forLanguageTag("tr"), Locale.forLanguageTag("az"))) {
                    Locale.setDefault(locale);
                    Person person = new PersonBuilder()
                            .withTags("zebra", "Alpha", "alpha", "10", "!tag")
                            .build();
                    PersonCard card = new PersonCard(person, 1, "-");

                    assertEquals(List.of("!tag", "10", "Alpha", "alpha", "zebra"), getTagNames(card),
                            "Unexpected tag order in locale " + locale);
                }
            } finally {
                Locale.setDefault(originalLocale);
            }
            return null;
        });
    }

    @Test
    public void personCard_studentWithoutOptionalDetails_displaysPlaceholders() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            Person student = new PersonBuilder().withoutEmail().withoutAddress().build();

            PersonCard card = new PersonCard(student, 1, "-");

            assertEquals("Guardian: -", getLabel(card, "relationship").getText());
            assertEquals("Guardian: -", getLabel(card, "relationship").getTooltip().getText());
            assertEquals("Address: -", getLabel(card, "address").getText());
            assertEquals("Email: -", getLabel(card, "email").getText());
            assertEquals("Address: -", getLabel(card, "address").getTooltip().getText());
            assertEquals("Email: -", getLabel(card, "email").getTooltip().getText());
            return null;
        });
    }

    @Test
    public void personCard_guardianWithoutLinkedStudents_displaysPlaceholder() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            Person guardian = new PersonBuilder().withRole(Role.GUARDIAN).build();

            PersonCard card = new PersonCard(guardian, 1, "-");

            assertEquals("GUARDIAN", getLabel(card, "role").getText());
            assertTrue(getLabel(card, "role").getStyleClass().contains("guardian"));
            assertEquals("Students: -", getLabel(card, "relationship").getText());
            assertEquals("Students: -", getLabel(card, "relationship").getTooltip().getText());
            assertTrue(getLabel(card, "relationship").isManaged());
            assertTrue(getLabel(card, "relationship").isVisible());
            return null;
        });
    }

    @Test
    public void personCard_guardianWithManyLinkedStudents_keepsRelationshipAccessibleAndCompact() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            String linkedStudentNames = IntStream.rangeClosed(1, 20)
                    .mapToObj(index -> "Student " + index)
                    .collect(Collectors.joining(", "));
            Person guardian = new PersonBuilder().withRole(Role.GUARDIAN).build();
            PersonCard card = new PersonCard(guardian, 1, linkedStudentNames);
            VBox cards = new VBox(card.getRoot());
            Stage stage = new Stage();

            try {
                stage.setScene(new Scene(cards, 340, 250));
                stage.show();
                layout(cards);

                Label relationship = getLabel(card, "relationship");
                String expectedText = "Students: " + linkedStudentNames;
                assertEquals(expectedText, relationship.getText());
                assertEquals(expectedText, relationship.getTooltip().getText());
                assertTrue(relationship.getTooltip().isWrapText());
                assertEquals(OverrunStyle.ELLIPSIS, relationship.getTextOverrun());
                assertFalse(relationship.isWrapText());
                assertTrue(relationship.prefWidth(-1) > relationship.getWidth());
                assertLabelStaysInsideCard(relationship, card);
            } finally {
                stage.close();
            }
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
            String guardianSummary = maximumLengthName + " (" + longPhone + ")";
            PersonCard card = new PersonCard(student, 1, guardianSummary);
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
                Label guardianName = getLabel(card, "relationship");
                Label address = getLabel(card, "address");
                Label email = getLabel(card, "email");
                assertEquals(maximumLengthName, name.getText());
                assertEquals("Phone: " + longPhone, phone.getText());
                assertEquals("Guardian: " + guardianSummary, guardianName.getText());
                assertEquals("Address: " + maximumLengthAddress, address.getText());
                assertEquals("Email: " + longEmail, email.getText());
                assertEquals(maximumLengthName, name.getTooltip().getText());
                assertEquals("Phone: " + longPhone, phone.getTooltip().getText());
                assertEquals("Guardian: " + guardianSummary, guardianName.getTooltip().getText());
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
    public void personCard_wrappedTags_growsWithoutOverlappingDetailsAndRelayoutsAfterResize() throws Exception {
        CardLayoutFixture fixture = JavaFxTestUtil.callOnFxThread(() -> {
            Person sixTagsPerson = new PersonBuilder()
                    .withTags("Active Tuition Student", "Secondary Four Level", "Additional Mathematics",
                            "English Language Study", "Physics Practical Prep", "Chemistry Practical Prep")
                    .build();
            Person oneTagPerson = new PersonBuilder().withTags("Active").build();
            Person noTagsPerson = new PersonBuilder().withTags().build();
            PersonCard sixTagsCard = new PersonCard(sixTagsPerson, 1, "-");
            PersonCard oneTagCard = new PersonCard(oneTagPerson, 2, "-");
            PersonCard noTagsCard = new PersonCard(noTagsPerson, 3, "-");
            VBox cards = new VBox(sixTagsCard.getRoot(), oneTagCard.getRoot(), noTagsCard.getRoot());
            Stage stage = new Stage();
            stage.setScene(new Scene(cards, TAG_RESIZED_WINDOW_WIDTH, TAG_OVERFLOW_SCENE_HEIGHT));
            stage.show();
            return new CardLayoutFixture(stage, sixTagsCard, oneTagCard, noTagsCard, cards);
        });

        try {
            assertTrue(JavaFxTestUtil.waitForFxCondition(() -> fixture.cards().getWidth() > 600));
            double wideSixTagHeight = JavaFxTestUtil.callOnFxThread(() -> {
                assertTagsDoNotOverlapPhone(fixture.sixTagsCard());
                return fixture.sixTagsCard().getRoot().getHeight();
            });

            JavaFxTestUtil.runOnFxThread(() -> fixture.stage().setWidth(TAG_OVERFLOW_SCENE_WIDTH));
            assertTrue(JavaFxTestUtil.waitForFxCondition(() -> fixture.cards().getWidth() < 400));
            double narrowSixTagHeight = JavaFxTestUtil.callOnFxThread(() -> {
                assertTagsDoNotOverlapPhone(fixture.sixTagsCard());
                return fixture.sixTagsCard().getRoot().getHeight();
            });
            JavaFxTestUtil.runOnFxThread(() -> {
                assertTrue(narrowSixTagHeight > wideSixTagHeight,
                        "Card did not grow when the tags wrapped after narrowing the window");
                assertTrue(narrowSixTagHeight > fixture.oneTagCard().getRoot().getHeight(),
                        "Card did not grow when tags wrapped onto additional rows");
                assertTrue(fixture.oneTagCard().getRoot().getHeight() >= fixture.noTagsCard().getRoot().getHeight(),
                        "A card with no tags should not reserve more tag space than a one-tag card");
            });

            JavaFxTestUtil.runOnFxThread(() -> fixture.stage().setWidth(TAG_RESIZED_WINDOW_WIDTH));
            assertTrue(JavaFxTestUtil.waitForFxCondition(() -> fixture.cards().getWidth() > 600));
            JavaFxTestUtil.runOnFxThread(() -> {
                assertTagsDoNotOverlapPhone(fixture.sixTagsCard());
                assertTrue(fixture.sixTagsCard().getRoot().getHeight() < narrowSixTagHeight,
                            "Card did not shrink after wrapped tags returned to fewer rows");
            });

            JavaFxTestUtil.runOnFxThread(() -> fixture.stage().setWidth(TAG_OVERFLOW_SCENE_WIDTH));
            assertTrue(JavaFxTestUtil.waitForFxCondition(() -> fixture.cards().getWidth() < 400));
            JavaFxTestUtil.runOnFxThread(() -> {
                assertTagsDoNotOverlapPhone(fixture.sixTagsCard());
                assertTrue(fixture.sixTagsCard().getRoot().getHeight()
                                >= narrowSixTagHeight - LAYOUT_BOUNDS_TOLERANCE,
                        "Card did not grow again after narrowing the window");
            });
        } finally {
            JavaFxTestUtil.runOnFxThread(fixture.stage()::close);
        }
    }

    @Test
    public void personListPanel_wrappedTags_doNotOverlapPhoneAtNarrowWidth() throws Exception {
        PanelLayoutFixture fixture = JavaFxTestUtil.callOnFxThread(() -> {
            Person sixTagsPerson = new PersonBuilder().withName("Six Tags")
                    .withTags("Active Tuition Student", "Secondary Four Level", "Additional Mathematics",
                            "English Language Study", "Physics Practical Prep", "Chemistry Practical Prep")
                    .build();
            Person nextPerson = new PersonBuilder().withName("Next Contact").withTags().build();
            ObservableList<Person> persons = FXCollections.observableArrayList(sixTagsPerson, nextPerson);
            PersonListPanel panel = new PersonListPanel(persons, persons);
            ListView<Person> personListView = getPersonListView(panel);
            Stage stage = new Stage();
            stage.setScene(new Scene(panel.getRoot(), TAG_RESIZED_WINDOW_WIDTH, TAG_OVERFLOW_SCENE_HEIGHT));
            stage.show();
            return new PanelLayoutFixture(stage, panel, personListView, sixTagsPerson, nextPerson);
        });

        try {
            assertTrue(JavaFxTestUtil.waitForFxCondition(() -> fixture.panel().getRoot().getWidth() > 600));
            JavaFxTestUtil.runOnFxThread(() -> fixture.stage().setWidth(TAG_OVERFLOW_SCENE_WIDTH));
            assertTrue(JavaFxTestUtil.waitForFxCondition(() -> fixture.panel().getRoot().getWidth() < 400));
            JavaFxTestUtil.runOnFxThread(() -> {
                ListCell<Person> sixTagsCell = getDisplayedCell(fixture.personListView(), fixture.sixTagsPerson());
                ListCell<Person> nextCell = getDisplayedCell(fixture.personListView(), fixture.nextPerson());
                assertTagsDoNotOverlapPhone(sixTagsCell);
                Bounds emailBounds = sixTagsCell.getGraphic().lookup("#email").localToScene(
                        sixTagsCell.getGraphic().lookup("#email").getBoundsInLocal());
                Bounds nextCardBounds = nextCell.getGraphic().localToScene(nextCell.getGraphic().getBoundsInLocal());
                assertTrue(emailBounds.getMaxY() <= nextCardBounds.getMinY() + LAYOUT_BOUNDS_TOLERANCE,
                        "Contact details extend into the neighbouring card");
            });
        } finally {
            JavaFxTestUtil.runOnFxThread(fixture.stage()::close);
        }
    }

    @Test
    public void personListPanel_guardianFilteredOut_displaysGuardianSummary() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("91234567")
                    .withRole(Role.GUARDIAN).build();
            Person student = new PersonBuilder().withName("Sam Student").withGuardian(guardian).build();
            ObservableList<Person> filteredPersons = FXCollections.observableArrayList(student);
            ObservableList<Person> allPersons = FXCollections.observableArrayList(student, guardian);

            PersonListPanel panel = new PersonListPanel(filteredPersons, allPersons);
            ListView<Person> personListView = getPersonListView(panel);
            ListCell<Person> studentCell = createCell(personListView, 0);

            assertSame(filteredPersons, personListView.getItems());
            assertEquals("Guardian: Grace Guardian (91234567)",
                    getLabel(studentCell, "relationship").getText());
            return null;
        });
    }

    @Test
    public void personListPanel_studentsFilteredOut_displaysLinkedStudentNames() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();
            Person firstStudent = new PersonBuilder().withName("Sam Student").withGuardian(guardian).build();
            Person secondStudent = new PersonBuilder().withName("Alex Student").withGuardian(guardian).build();
            ObservableList<Person> filteredPersons = FXCollections.observableArrayList(guardian);
            ObservableList<Person> allPersons = FXCollections.observableArrayList(
                    guardian, firstStudent, secondStudent);

            PersonListPanel panel = new PersonListPanel(filteredPersons, allPersons);
            ListView<Person> personListView = getPersonListView(panel);
            ListCell<Person> guardianCell = createCell(personListView, 0);

            assertSame(filteredPersons, personListView.getItems());
            assertEquals("Students: Sam Student, Alex Student",
                    getLabel(guardianCell, "relationship").getText());
            return null;
        });
    }

    @Test
    public void personListPanel_allPersonsChanges_refreshGuardianSummary() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("91234567")
                    .withRole(Role.GUARDIAN).build();
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

                assertEquals("Guardian: -", getLabel(displayedStudentCell, "relationship").getText());

                allPersons.add(guardian);
                displayedStudentCell = getDisplayedCell(personListView, student);

                assertEquals("Guardian: Grace Guardian (91234567)",
                        getLabel(displayedStudentCell, "relationship").getText());

                Person updatedGuardian = new PersonBuilder(guardian).withName("Grace Lim")
                        .withPhone("92345678").build();
                allPersons.set(1, updatedGuardian);
                displayedStudentCell = getDisplayedCell(personListView, student);

                assertEquals("Guardian: Grace Lim (92345678)",
                        getLabel(displayedStudentCell, "relationship").getText());

                allPersons.remove(updatedGuardian);
                displayedStudentCell = getDisplayedCell(personListView, student);

                assertEquals("Guardian: -", getLabel(displayedStudentCell, "relationship").getText());
            } finally {
                stage.close();
            }
            return null;
        });
    }

    @Test
    public void personListPanel_allPersonsChanges_refreshLinkedStudentNames() throws Exception {
        JavaFxTestUtil.callOnFxThread(() -> {
            Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();
            Person student = new PersonBuilder().withName("Sam Student").withGuardian(guardian).build();
            ObservableList<Person> filteredPersons = FXCollections.observableArrayList(guardian);
            ObservableList<Person> allPersons = FXCollections.observableArrayList(guardian);
            PersonListPanel panel = new PersonListPanel(filteredPersons, allPersons);
            ListView<Person> personListView = getPersonListView(panel);
            Stage stage = new Stage();

            try {
                stage.setScene(new Scene(panel.getRoot(), 400, 300));
                stage.show();
                ListCell<Person> displayedGuardianCell = getDisplayedCell(personListView, guardian);

                assertEquals("Students: -", getLabel(displayedGuardianCell, "relationship").getText());

                allPersons.add(student);
                displayedGuardianCell = getDisplayedCell(personListView, guardian);

                assertEquals("Students: Sam Student",
                        getLabel(displayedGuardianCell, "relationship").getText());

                Person renamedStudent = new PersonBuilder(student).withName("Alex Student").build();
                allPersons.set(1, renamedStudent);
                displayedGuardianCell = getDisplayedCell(personListView, guardian);

                assertEquals("Students: Alex Student",
                        getLabel(displayedGuardianCell, "relationship").getText());

                allPersons.remove(renamedStudent);
                displayedGuardianCell = getDisplayedCell(personListView, guardian);

                assertEquals("Students: -", getLabel(displayedGuardianCell, "relationship").getText());
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

    private static void assertTagsDoNotOverlapPhone(PersonCard card) {
        FlowPane tags = (FlowPane) card.getRoot().lookup("#tags");
        Label phone = getLabel(card, "phone");
        Bounds phoneBounds = phone.localToScene(phone.getBoundsInLocal());
        for (Label tagLabel : getTagLabels(card)) {
            Bounds tagBounds = tagLabel.localToScene(tagLabel.getBoundsInLocal());
            assertTrue(tagBounds.getMaxY() <= phoneBounds.getMinY() + LAYOUT_BOUNDS_TOLERANCE,
                    "Wrapped tag overlaps phone: " + tagBounds + " vs " + phoneBounds);
        }
    }

    private static void assertTagsDoNotOverlapPhone(ListCell<Person> cell) {
        FlowPane tags = (FlowPane) cell.getGraphic().lookup("#tags");
        Label phone = (Label) cell.getGraphic().lookup("#phone");
        Bounds phoneBounds = phone.localToScene(phone.getBoundsInLocal());
        for (javafx.scene.Node tagNode : tags.getChildren()) {
            Bounds tagBounds = tagNode.localToScene(tagNode.getBoundsInLocal());
            assertTrue(tagBounds.getMaxY() <= phoneBounds.getMinY() + LAYOUT_BOUNDS_TOLERANCE,
                    "Wrapped tag overlaps phone: " + tagBounds + " vs " + phoneBounds);
        }
    }

    private static void assertLabelStaysInsideCard(Label label, PersonCard card) {
        Bounds cardBounds = card.getRoot().localToScene(card.getRoot().getBoundsInLocal());
        Bounds labelBounds = label.localToScene(label.getBoundsInLocal());
        assertTrue(labelBounds.getMinX() >= cardBounds.getMinX() - LAYOUT_BOUNDS_TOLERANCE);
        assertTrue(labelBounds.getMaxX() <= cardBounds.getMaxX() + LAYOUT_BOUNDS_TOLERANCE,
                "Label extends past card: " + labelBounds + " vs " + cardBounds);
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

    private record CardLayoutFixture(Stage stage, PersonCard sixTagsCard, PersonCard oneTagCard,
                                     PersonCard noTagsCard, VBox cards) {
    }

    private record PanelLayoutFixture(Stage stage, PersonListPanel panel, ListView<Person> personListView,
                                      Person sixTagsPerson, Person nextPerson) {
    }
}
