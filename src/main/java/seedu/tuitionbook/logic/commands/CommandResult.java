package seedu.tuitionbook.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import seedu.tuitionbook.commons.util.ToStringBuilder;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    /** Details that should replace the contact list, if any. */
    private final String detailsToShow;

    /** Contact that should be scrolled into the visible viewport, if any. */
    private final UUID personIdToScrollTo;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, null, null);
    }

    /**
     * Constructs a {@code CommandResult} with feedback and details to display in the main content area.
     */
    public CommandResult(String feedbackToUser, String detailsToShow) {
        this(feedbackToUser, false, false, requireNonNull(detailsToShow), null);
    }

    /**
     * Constructs a {@code CommandResult} that requests scrolling the identified contact into view.
     *
     * @param feedbackToUser Feedback to display to the user.
     * @param personIdToScrollTo ID of the contact to scroll into the visible viewport.
     */
    public CommandResult(String feedbackToUser, UUID personIdToScrollTo) {
        this(feedbackToUser, false, false, null, requireNonNull(personIdToScrollTo));
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, String detailsToShow,
            UUID personIdToScrollTo) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.detailsToShow = detailsToShow;
        this.personIdToScrollTo = personIdToScrollTo;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    /**
     * Returns the details to display in the main content area, if the command supplied any.
     */
    public Optional<String> getDetailsToShow() {
        return Optional.ofNullable(detailsToShow);
    }

    /**
     * Returns the ID of the contact to scroll into the visible viewport, if the command supplied one.
     */
    public Optional<UUID> getPersonIdToScrollTo() {
        return Optional.ofNullable(personIdToScrollTo);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && Objects.equals(detailsToShow, otherCommandResult.detailsToShow)
                && Objects.equals(personIdToScrollTo, otherCommandResult.personIdToScrollTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, detailsToShow, personIdToScrollTo);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("detailsToShow", detailsToShow)
                .add("personIdToScrollTo", personIdToScrollTo)
                .toString();
    }

}
