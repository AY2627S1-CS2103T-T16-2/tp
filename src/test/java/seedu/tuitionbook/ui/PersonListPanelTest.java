package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.stage.Stage;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.PersonBuilder;

public class PersonListPanelTest {

    private static final Bounds VIEWPORT = new BoundingBox(0, 0, 100, 200);

    @Test
    public void resolveGuardianSummary_linkedStudent_returnsGuardianNameAndPhone() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("91234567")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withGuardian(guardian).build();

        String result = PersonListPanel.resolveGuardianSummary(student, Map.of(guardian.getId(), guardian));

        assertEquals("Grace Guardian (91234567)", result);
    }

    @Test
    public void resolveGuardianSummary_unlinkedStudent_returnsPlaceholder() {
        Person student = new PersonBuilder().withName("Sam Student").build();

        assertEquals("-", PersonListPanel.resolveGuardianSummary(student, Map.of(student.getId(), student)));
    }

    @Test
    public void resolveGuardianSummary_guardianContact_returnsPlaceholder() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();

        assertEquals("-", PersonListPanel.resolveGuardianSummary(guardian, Map.of(guardian.getId(), guardian)));
    }

    @Test
    public void resolveGuardianSummary_missingGuardian_returnsPlaceholder() {
        Person student = new PersonBuilder().withName("Sam Student").withGuardianId(UUID.randomUUID()).build();

        assertEquals("-", PersonListPanel.resolveGuardianSummary(student, Map.of(student.getId(), student)));
    }

    @Test
    public void resolveGuardianSummary_guardianIdRefersToStudent_returnsPlaceholder() {
        Person referencedStudent = new PersonBuilder().withName("Other Student").build();
        Person student = new PersonBuilder().withName("Sam Student").withGuardianId(referencedStudent.getId()).build();

        assertEquals("-", PersonListPanel.resolveGuardianSummary(student,
                Map.of(referencedStudent.getId(), referencedStudent)));
    }

    @Test
    public void resolveGuardianSummary_guardianExcludedFromDisplayedPersons_returnsNameAndPhone() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("91234567")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withGuardian(guardian).build();
        List<Person> displayedPersons = List.of(student);

        assertFalse(displayedPersons.contains(guardian));
        assertEquals("Grace Guardian (91234567)",
                PersonListPanel.resolveGuardianSummary(student, Map.of(guardian.getId(), guardian)));
    }

    @Test
    public void resolveLinkedStudentNames_guardianWithoutLinkedStudents_returnsPlaceholder() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();
        Person unlinkedStudent = new PersonBuilder().withName("Una Student").build();

        assertEquals("-", PersonListPanel.resolveLinkedStudentNames(guardian,
                List.of(guardian, unlinkedStudent)));
    }

    @Test
    public void resolveLinkedStudentNames_guardianWithOneLinkedStudent_returnsStudentName() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();
        Person linkedStudent = new PersonBuilder().withName("Sam Student").withGuardian(guardian).build();

        assertEquals("Sam Student", PersonListPanel.resolveLinkedStudentNames(guardian,
                List.of(guardian, linkedStudent)));
    }

    @Test
    public void resolveLinkedStudentNames_multipleContacts_returnsLinkedStudentsInContactListOrder() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();
        Person otherGuardian = new PersonBuilder().withName("Gina Guardian").withRole(Role.GUARDIAN).build();
        Person secondLinkedStudent = new PersonBuilder().withName("Zara Student").withGuardian(guardian).build();
        Person unrelatedStudent = new PersonBuilder().withName("Una Student").withGuardian(otherGuardian).build();
        Person firstLinkedStudent = new PersonBuilder().withName("Alex Student").withGuardian(guardian).build();

        assertEquals("Zara Student, Alex Student", PersonListPanel.resolveLinkedStudentNames(guardian,
                List.of(secondLinkedStudent, unrelatedStudent, firstLinkedStudent, guardian, otherGuardian)));
    }

    @Test
    public void resolveLinkedStudentNames_studentContact_returnsPlaceholder() {
        Person student = new PersonBuilder().withName("Sam Student").build();

        assertEquals("-", PersonListPanel.resolveLinkedStudentNames(student, List.of(student)));
    }

    @Test
    public void isCellWithinViewport_cellFullyInside_returnsTrue() {
        assertTrue(PersonListPanel.isCellWithinViewport(VIEWPORT, new BoundingBox(10, 10, 80, 100)));
    }

    @Test
    public void isCellWithinViewport_cellJustOutsideWithinTolerance_returnsTrue() {
        assertTrue(PersonListPanel.isCellWithinViewport(VIEWPORT, new BoundingBox(-1, -1, 102, 202)));
    }

    @Test
    public void isCellWithinViewport_cellPastLeftEdge_returnsFalse() {
        assertFalse(PersonListPanel.isCellWithinViewport(VIEWPORT, new BoundingBox(-10, 10, 80, 100)));
    }

    @Test
    public void isCellWithinViewport_cellPastRightEdge_returnsFalse() {
        assertFalse(PersonListPanel.isCellWithinViewport(VIEWPORT, new BoundingBox(30, 10, 80, 100)));
    }

    @Test
    public void isCellWithinViewport_cellPastTopEdge_returnsFalse() {
        assertFalse(PersonListPanel.isCellWithinViewport(VIEWPORT, new BoundingBox(10, -10, 80, 100)));
    }

    @Test
    public void isCellWithinViewport_cellPastBottomEdge_returnsFalse() {
        assertFalse(PersonListPanel.isCellWithinViewport(VIEWPORT, new BoundingBox(10, 150, 80, 100)));
    }

    @Test
    public void isCellWithinViewport_tallerCellOverlappingViewport_returnsTrue() {
        assertTrue(PersonListPanel.isCellWithinViewport(VIEWPORT, new BoundingBox(10, -50, 80, 300)));
    }

    @Test
    public void isCellWithinViewport_tallerCellBelowViewport_returnsFalse() {
        assertFalse(PersonListPanel.isCellWithinViewport(VIEWPORT, new BoundingBox(10, 300, 80, 300)));
    }

    @Test
    public void isCellWithinViewport_tallerOverlappingCellPastHorizontalEdge_returnsFalse() {
        assertFalse(PersonListPanel.isCellWithinViewport(VIEWPORT, new BoundingBox(-10, -50, 80, 300)));
    }

    @Test
    public void scrollTowardsPerson_flowWithoutLaidOutCells_fallsBackToScrollTo() throws Exception {
        JavaFxTestUtil.initialize();
        JavaFxTestUtil.runOnFxThread(() -> {
            ObservableList<Person> people = buildPeople(10);
            PersonListPanel panel = new PersonListPanel(people, people);
            new Scene(panel.getRoot());
            panel.getRoot().applyCss();

            assertTrue(panel.scrollTowardsPerson(people.get(5).getId()));
        });
    }

    @Test
    public void scrollTowardsPerson_targetOutsideRealisedRows_stepsTowardsTarget() throws Exception {
        ShownPanelFixture fixture = createShownPanelFixture(30);
        try {
            JavaFxTestUtil.runOnFxThread(() -> {
                VirtualFlow<?> virtualFlow = (VirtualFlow<?>) fixture.listView().lookup(".virtual-flow");
                virtualFlow.scrollPixels(Double.MAX_VALUE);
                fixture.listView().layout();
                int firstVisibleIndex = virtualFlow.getFirstVisibleCell().getIndex();
                assertTrue(firstVisibleIndex > 0, "The list should start scrolled to its bottom");

                assertTrue(fixture.panel().scrollTowardsPerson(fixture.people().get(0).getId()));
                fixture.listView().layout();
                assertTrue(virtualFlow.getFirstVisibleCell().getIndex() < firstVisibleIndex,
                        "A target above the realised rows should step the viewport upwards");

                assertTrue(fixture.panel().scrollTowardsPerson(
                        fixture.people().get(virtualFlow.getFirstVisibleCell().getIndex()).getId()),
                        "A target within the realised rows should be scrolled to directly");
            });
        } finally {
            JavaFxTestUtil.runOnFxThread(fixture.stage()::hide);
        }
    }

    @Test
    public void isPersonFullyVisible_rowPartiallyScrolledOutOfViewport_returnsFalse() throws Exception {
        ShownPanelFixture fixture = createShownPanelFixture(30);
        try {
            JavaFxTestUtil.runOnFxThread(() -> {
                assertTrue(fixture.panel().isPersonFullyVisible(fixture.people().get(0).getId()),
                        "The first contact should be fully visible before scrolling");

                VirtualFlow<?> virtualFlow = (VirtualFlow<?>) fixture.listView().lookup(".virtual-flow");
                virtualFlow.scrollPixels(10);
                fixture.listView().layout();

                assertFalse(fixture.panel().isPersonFullyVisible(fixture.people().get(0).getId()),
                        "A row scrolled partially out of the viewport should not count as visible");
                assertTrue(fixture.panel().isPersonFullyVisible(fixture.people().get(1).getId()),
                        "A row still fully inside the viewport should count as visible");
            });
        } finally {
            JavaFxTestUtil.runOnFxThread(fixture.stage()::hide);
        }
    }

    private static ShownPanelFixture createShownPanelFixture(int contactCount) throws Exception {
        JavaFxTestUtil.initialize();
        ShownPanelFixture fixture = JavaFxTestUtil.callOnFxThread(() -> {
            ObservableList<Person> people = buildPeople(contactCount);
            PersonListPanel panel = new PersonListPanel(people, people);
            Stage stage = new Stage();
            stage.setScene(new Scene(panel.getRoot(), 700, 300));
            stage.show();
            ListView<?> listView = (ListView<?>) panel.getRoot().lookup("#personListView");
            return new ShownPanelFixture(stage, panel, listView, people);
        });
        assertTrue(JavaFxTestUtil.waitForFxCondition(() ->
                fixture.listView().getSkin() != null && fixture.listView().getHeight() > 0));
        return fixture;
    }

    private record ShownPanelFixture(Stage stage, PersonListPanel panel, ListView<?> listView,
                                     ObservableList<Person> people) {
    }

    private static ObservableList<Person> buildPeople(int contactCount) {
        return FXCollections.observableArrayList(IntStream.range(0, contactCount)
                .mapToObj(index -> new PersonBuilder().withName("Existing Student " + index)
                        .withPhone(String.format("8%07d", index)).build())
                .toList());
    }
}
