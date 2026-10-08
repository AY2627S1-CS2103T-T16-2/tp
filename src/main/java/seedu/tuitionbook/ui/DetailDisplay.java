package seedu.tuitionbook.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays contact and relationship details in the main content area.
 */
public class DetailDisplay extends UiPart<Region> {

    private static final String FXML = "DetailDisplay.fxml";

    @FXML
    private TextArea detailDisplay;

    public DetailDisplay() {
        super(FXML);
    }

    /**
     * Replaces the displayed details with {@code details}.
     */
    public void setDetails(String details) {
        detailDisplay.setText(requireNonNull(details));
        detailDisplay.positionCaret(0);
    }
}
