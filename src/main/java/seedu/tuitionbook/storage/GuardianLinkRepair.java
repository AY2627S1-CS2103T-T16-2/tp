package seedu.tuitionbook.storage;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

/** Describes a guardian link that was removed while loading contact data. */
public record GuardianLinkRepair(String personName, UUID personId, String guardianId, Reason reason) {

    /** The reasons why a guardian link may be removed during loading. */
    public enum Reason {
        MALFORMED_GUARDIAN_ID("the guardian ID is malformed"),
        MISSING_GUARDIAN("the referenced contact does not exist"),
        STUDENT_GUARDIAN("the referenced contact is a student"),
        SELF_LINK("the student refers to itself"),
        NON_STUDENT_GUARDIAN_LINK("the contact is not a student");

        private final String description;

        Reason(String description) {
            this.description = description;
        }

        /** Returns a user-facing explanation of this repair reason. */
        public String getDescription() {
            return description;
        }
    }

    /** Validates this repair diagnostic. */
    public GuardianLinkRepair {
        requireNonNull(personName);
        requireNonNull(personId);
        requireNonNull(reason);
    }
}
