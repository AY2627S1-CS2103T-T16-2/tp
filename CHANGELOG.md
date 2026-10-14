# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog.

- Keep a Changelog: https://keepachangelog.com/en/1.1.0/

## [v1.3] - 2026-10-14

### Added

- Added student and guardian roles for contacts.
- Added the `link` command for associating students with guardians.
- Added role-based listing and whole-word name searching.
- Added detailed contact views, including guardian relationships.
- Added automatic data migration, persistence, and recovery for invalid data files.
- Added duplicate checking after contact edits.
- Added guardian replacement and idempotent re-linking behavior.

### Changed

- Made email addresses and home addresses optional when adding or editing contacts.
- Updated `add` and `edit` to support the new contact fields and validation rules.
- Changed `edit` to allow explicit clearing of email addresses and home addresses.
- Preserved support for tags while allowing them to be cleared during editing.
- Improved contact cards to display roles, relationships, and long values clearly.
- Improved validation messages for invalid commands, indexes, roles, duplicate fields, and field values.
- Updated the User Guide and help content for TuitionBook commands and data handling.

### Screenshots

![Managing students and guardians](https://raw.githubusercontent.com/AY2627S1-CS2103T-T16-2/tp/master/docs/images/v1.3-manage-studentandguardian.jpeg)
Manage student and guardian contacts together, with linked guardians and subject tags visible.

![Viewing contact details](https://raw.githubusercontent.com/AY2627S1-CS2103T-T16-2/tp/master/docs/images/v1.3-viewdetails.jpeg)
View a student’s details and their guardian’s contact information in one command.

![Linking a guardian](https://raw.githubusercontent.com/AY2627S1-CS2103T-T16-2/tp/master/docs/images/v1.3-guardianlinking.jpeg)
A guardian can be linked to multiple students, such as siblings.

![Filtering contacts](https://raw.githubusercontent.com/AY2627S1-CS2103T-T16-2/tp/master/docs/images/v1.3-viewfiltering.jpeg)
Filter the list to students while keeping their guardian relationships visible.
