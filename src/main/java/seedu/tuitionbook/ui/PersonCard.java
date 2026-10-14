package seedu.tuitionbook.ui;

import java.util.Comparator;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final double TOOLTIP_MAX_WIDTH = 600;

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/AY2627S1-CS2103T-T16-2/tp">TuitionBook project</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label role;
    @FXML
    private Label phone;
    @FXML
    private Label guardian;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private FlowPane tags;

    /**
     * Creates a {@code PersonCard} with the given {@code Person}, index, and resolved guardian name to display.
     *
     * @param person person whose details are displayed
     * @param displayedIndex one-based index shown on the card
     * @param guardianName resolved guardian name or {@code "-"}
     */
    public PersonCard(Person person, int displayedIndex, String guardianName) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        name.setTooltip(createFullValueTooltip(person.getName().fullName));
        role.setText(person.getRole().name());
        role.getStyleClass().add(person.getRole().name().toLowerCase(Locale.ROOT));
        phone.setText("Phone: " + person.getPhone().value);
        phone.setTooltip(createFullValueTooltip(phone.getText()));
        boolean isStudent = person.getRole() == Role.STUDENT;
        guardian.setManaged(isStudent);
        guardian.setVisible(isStudent);
        if (isStudent) {
            guardian.setText("Guardian: " + guardianName);
            guardian.setTooltip(createFullValueTooltip(guardian.getText()));
        }
        address.setText("Address: " + person.getAddress().map(value -> value.value).orElse("-"));
        address.setTooltip(createFullValueTooltip(address.getText()));
        email.setText("Email: " + person.getEmail().map(value -> value.value).orElse("-"));
        email.setTooltip(createFullValueTooltip(email.getText()));
        tags.setMinWidth(0);
        tags.setMaxWidth(Double.MAX_VALUE);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .map(tag -> createTagLabel(tag.tagName))
                .forEach(tags.getChildren()::add);
    }

    private Label createTagLabel(String tagName) {
        Label tagLabel = new Label(tagName);
        tagLabel.setMinWidth(0);
        tagLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        tagLabel.setTooltip(createFullValueTooltip(tagName));
        tagLabel.maxWidthProperty().bind(tags.widthProperty());
        return tagLabel;
    }

    private static Tooltip createFullValueTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(TOOLTIP_MAX_WIDTH);
        return tooltip;
    }
}
