# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog.

- Keep a Changelog: https://keepachangelog.com/en/1.1.0/

## [Unreleased]

### Added

- Support quoted addresses in `add` and `edit`, protecting complete addresses containing prefix-like text and using the same quote/backslash escapes as names and tags (#201).

### Changed

- Align the UG and DG descriptions of indexes, unchanged commands, reload and save-failure recovery with exact working-set membership, order and query; share the full-list refresh command across model, commands and UI.
- Clarify that `list` locates hidden additions while rerunning the originating query only refreshes its working set; include ordered displayed UUIDs, query predicates and refresh text when comparing models.
- Accept command words in any letter case without changing field prefixes, values or the lowercase `confirm` keyword.
- Acknowledged in the Developer Guide the bug reports from team T16-1's TAB project that were adapted, with that team's approval, as bug-finding test scenarios for TuitionBook.
- Documented save-failure rollback in the Delete use case, including restoration of the contact, guardian links, and active filter.
- Added a Developer Guide use case for reloading contact data, including missing-file, repaired-link, successful-backup, and blocked-saving outcomes.
- Expanded the documented contact-field validation subset to support right curly apostrophes in names; phone groups separated by spaces, hyphens, or slashes, optional trailing phone annotations, and numeric extensions; apostrophes and punycode labels in email addresses; and explicitly single-line addresses.
- Clarified that names accept ordinary ASCII spaces (U+0020), rather than every Unicode whitespace character.
- Pasted tabs and line separators in the command box are converted to spaces so that adjacent words remain separate; pasted text is not executed until Enter is pressed.
- Preserve an ordered contact working set across edit, tag, untag, link and unlink; retain and mark contacts that stop matching with canonical refresh commands, append matching additions, explain hidden additions, and warn when deletion compacts indexes. Explicit queries refresh membership; reload, clear and restart reset it. Save failures restore exact membership, order and query (#187).
- Clarified the supported `guardianId` JSON value types and the difference between link repair and whole-file recovery.

### Fixed

- Show the fatal startup error dialog when a packaged UI resource is missing or malformed, instead of raising a secondary null-window exception.
- Rejected whitespace-, control-, or format-only and line-breaking addresses consistently when entered through commands or loaded from JSON.
- Normalized phone spaces, hyphens, slashes, numeric-group parentheses, annotations, and equivalent extension markers during duplicate detection while retaining a leading `+` and extension digits.
- Reported every missing mandatory `add` field in role, name, phone order, and showed the ordinary example before quoted-prefix guidance.
- Made duplicate name identity consistently compare `Groß` with `GROẞ`, while preserving the existing normalized-name-and-phone identity rules and original display spelling.
- Made literal name-substring search use the same locale-independent Unicode comparison form for stored names and keywords, so composed/decomposed accents and the demonstrated sharp-S cases match consistently without applying duplicate identity's space normalization.
- Focused the command box after startup and after dismissing a startup notice so typing is immediately available without accidentally activating Help.
- Allowed contact cards to grow and shrink with wrapped tag rows, preventing tags from obscuring contact details or neighbouring cards at narrow widths.
- Split the dense delete sequence diagram into focused parsing and execution diagrams that remain readable at normal browser zoom.
- Reject unknown space-separated ASCII-letter prefixes in `add`, `edit`, `list`, `tag` and `untag` rather than silently absorbing mistyped prefixes into field values; intentional literal text must be quoted (#193). Stored values, field validation, tag identity and whitespace trimming are unchanged.
- Scroll the contact list to a newly added contact so it remains visible when the list extends beyond the viewport.
- Use locale-independent, case-insensitive alphabetical ordering for tags in contact cards and detailed views.
- Render detailed-view tags with brackets and escape literal backslashes and square brackets.
- Keep the current screen visible when a command fails instead of forcing a return to the contact list.
- Give bare and invalid `clear` confirmations distinct guidance without changing `clear confirm` behaviour.
- Reject extra arguments to `help` and `exit`, consistently with `reload`, rather than opening Help or exiting unexpectedly.
- Report raw line-break code points and correction guidance when input reaches the parser without command-box conversion, and diagnose non-breaking/full-width spaces in command words and indexes without normalising those characters in field values.
- Update the UG, DG manual tests and offline Help to describe the development build's command-input rules rather than the old permissive behaviour.
- Reject JSON files containing trailing content or duplicate property names so that ambiguous data is preserved through the existing malformed-file recovery workflow instead of being silently discarded on the next save.
- Continue startup with the unchanged legacy `addressbook.json` when permissions prevent migration to `tuitionbook.json`, allowing migration to be retried on a later launch.
- Decode JSON files strictly as UTF-8 so UTF-8 BOMs are accepted while invalid UTF-8 and other encodings are rejected and preserved unchanged through recovery.
- Report permission, encoding/JSON, invalid contact-value, and other file-system load failures with appropriate startup and reload recovery guidance.

## [v1.4] - 2026-10-22

### Added

- Added `tag` and `untag` to add or remove selected tags without retyping the contact's entire tag set; `tag` lists only newly added tags in its success feedback, and `untag` reports requested tags that were not present. `add` still supplies initial tags and `edit` still replaces all tags.
- Added optional double-quoted names and tags, allowing values such as `n/"Rina a/p Kaur"` and `t/"call p/next week"` without interpreting embedded field prefixes in `add`, `edit`, and `list`. Unquoted field parsing remains unchanged.
- Added `\"` and `\\` escapes inside quoted values, allowing literal quotes and backslashes in tags to be entered, filtered, and removed consistently through `add`, `edit`, `list`, `tag`, and `untag`, including tags loaded from existing JSON files.
- Added a scrollable in-app summary of every implemented command and its syntax.
- Added an `Open User Guide` action that uses the system default browser and shows copy-link fallback guidance when opening fails.
- Added the `unlink` command to remove a student's guardian link without deleting either contact.
- Added linked student names to guardian cards and guardian names and contact numbers to student cards, with compact overflow handling for long relationship summaries.
- Added `reload` to reread the configured contact file and retry recovery from blocked saving without restarting.

### Changed

- Improved guide navigation, first-use instructions, troubleshooting and developer reading paths; replaced outdated UG screenshots with explicit outcomes and added a documentation maintenance checklist.

- Reconciled the v1.4 scope decisions with implemented commands, including quoted-tag escapes, incremental tag changes and saves only after actual contact changes, documented level/subject/status tag workflows and single-instance/manual-editing cautions, and removed unsupported future undo and archiving promises.
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

- Corrected the UG guidance on arguments to `reload` and `clear`, and updated the delete sequence diagram and its accompanying explanation to match current execution.
- Corrected the Developer Guide parser class diagram and explanation to reflect command-specific parsers, shared tag parsing, direct command creation, and current utility dependencies.
- Save contact data only when commands report an actual change through `requiresSave()`, avoiding a full-list comparison, unnecessary writes, and misleading permission errors for repeated links, unchanged field or tag edits, unlinking unlinked students, and clearing an empty contact list. Save decisions reset on every command execution, while rollback snapshots remain in place.
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
