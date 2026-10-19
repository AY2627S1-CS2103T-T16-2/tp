# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog.

- Keep a Changelog: https://keepachangelog.com/en/1.1.0/

## [v1.4] - 2026-10-22

### Added

- Added optional double-quoted names to `add` and `edit`, allowing names such as `n/"Rina a/p Kaur"` without interpreting embedded field prefixes. Unquoted field parsing remains unchanged.
- Added a scrollable in-app summary of every implemented command and its syntax.
- Added an `Open User Guide` action that uses the system default browser and shows copy-link fallback guidance when opening fails.
- Added the `unlink` command to remove a student's guardian link without deleting either contact.
- Added linked student names to guardian cards and guardian names and contact numbers to student cards, with compact overflow handling for long relationship summaries.
- Added `reload` to reread the configured contact file and retry recovery from blocked saving without restarting.

### Changed

- Moved the User Guide command summary before Quick start and corrected its sidebar link.
- Replaced the nested Help menu with a one-click top-level Help control while retaining the `help` command, <kbd>F1</kbd> shortcut, and `Copy URL` action.
- Clarified that `link` indexes refer to the currently displayed, filtered contact list.
- Changed `list` to allow tag filtering, includes case-insensitive AND matching with the optional role filtering
- Allowed spaces and printable ASCII punctuation in non-blank tag names, including repeated hyphens, while preserving exact case-insensitive tag filtering and existing field-prefix boundaries.
- Changed `find` to use Unicode-aware, case-insensitive literal substring matching anywhere in contact names while preserving name-only OR search behavior.
- Required `clear confirm` before clearing all contacts and rejected every other argument to `clear`.
- Restored contacts and the active filter when a contact-changing command cannot save its changes.
- Changed `view` to show linked contact names and contact numbers by default and accept `v/` for their full contact details.

### Fixed

- Treat inaccessible JSON files as loading errors rather than missing files, preventing reload from clearing contacts when file existence cannot be determined.

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
