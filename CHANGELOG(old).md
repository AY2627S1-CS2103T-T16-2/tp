# TuitionBook v1.3 — Initial release

TuitionBook is a contact-management application for private one-to-one home
tutoring. This release adapts the AB3 address-book foundation to support the
TuitionBook MVP.

## What's new compared to AB3

### Contacts for tutoring

- Contacts now have an explicit role: `student` or `guardian`.
- `add` accepts the required role, name, and phone number, together with
  optional email address, home address, and tags.
- Email address and home address are optional. Missing values are displayed as
  `-` instead of preventing a contact from being created.
- Names and addresses support the TuitionBook validation and length limits,
  including Unicode-aware character counting and preserved meaningful address
  text.
- Phone numbers support a leading `+` and limited spaces or hyphens while
  retaining their entered formatting.
- Contact cards display role badges and labelled phone, email, address, and
  guardian information. Long values are shortened with an ellipsis and can be
  read in full using a tooltip or the `view` command.

### Student–guardian relationships

- Added the `link STUDENT_INDEX GUARDIAN_INDEX` command for associating a
  student with a guardian.
- Linking validates both displayed indexes and both roles, and reports which
  index is invalid when a command cannot be executed.
- Linking a student to a different guardian replaces the old relationship and
  reports the previous guardian that was unlinked.
- Repeating an existing student–guardian link is idempotent: it reports that
  the relationship already exists without changing the contact list or filter.
- `view` shows a student's linked guardian or a guardian's linked students,
  including related contacts that are hidden by the current filter.
- Deleting a guardian or student cleans up the corresponding guardian link.

### Editing and duplicate detection

- `edit` now supports optional email and address fields while preserving
  fields that are omitted from the command.
- An existing email or address can be cleared explicitly with `e/` or `a/`.
- Editing cannot change a contact's role or guardian link; use `link` for
  guardian relationships.
- Post-edit values are checked against the duplicate-contact rule before the
  edit is applied.
- Duplicate detection compares normalized names and phone numbers, ignoring
  case, surrounding/repeated spaces, and phone separators while retaining
  meaningful distinctions such as a leading `+`.
- Tags can be replaced during editing, and `t/` clears all existing tags.

### Searching and displaying contacts

- `list` can filter contacts by role and clears previous name or role filters
  when showing the complete list.
- `find` performs case-insensitive, whole-word name searches with OR semantics
  for multiple keywords.
- `view INDEX` provides a keyboard-accessible full-detail view, including
  optional fields and relationship information, without modifying contact
  data.
- Contact-changing commands restore the full contact list after a successful
  operation where necessary; read-only commands preserve contact data.

### Data storage and recovery

- Contact data is saved automatically to `data/tuitionbook.json` after commands
  that change contacts or guardian links.
- Existing AB3 data is migrated from `data/addressbook.json` on first launch
  after upgrading.
- Persisted contacts now include stable IDs, roles, optional fields, tags, and
  guardian relationships.
- Malformed or invalid data is handled with a startup warning and a preserved
  sibling backup when possible, allowing the application to recover with a
  usable data file.
- Invalid guardian references are repaired on load rather than being allowed
  to corrupt the in-memory relationship model.

### User experience and command validation

- The GUI now labels contact information clearly and keeps contact cards usable
  for long names, phone numbers, email addresses, addresses, and guardian
  names.
- Help and the User Guide document the TuitionBook commands, optional fields,
  linking behavior, data-file format, and recovery behavior.
- Invalid command formats, indexes, roles, duplicate fields, and invalid field
  values produce targeted error messages and usage guidance.
- The application is packaged as an executable JAR and released automatically
  through GitHub Actions using these release notes.
