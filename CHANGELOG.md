# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog.

- Keep a Changelog: https://keepachangelog.com/en/1.1.0/

## [v1.5] - 2026-10-28

### Added

- Prevent multiple TuitionBook instances from using the same application home; copies in separate folders can run independently.
- Show the active query and displayed, matching and retained contact counts above the contact list.
- Support double-quoted addresses in `add` and `edit`, including escaped quotes and backslashes.

### Changed

- Preserve contact order and indexes across edits, tag changes and guardian-link changes. Mark contacts that no longer match the query, append matching additions and explain hidden additions. Explicit queries refresh the list; reload, clear and restart reset it.
- Accept command words in any letter case while keeping field prefixes and `confirm` lowercase.
- Expand supported name, phone and email formats, including curly apostrophes in names, phone separators and extensions, and punctuation in email addresses.
- Simplify command feedback, validation errors, the User Guide and in-app Help, including guidance on quoted values and save-failure rollback.
- Convert pasted tabs and line separators to spaces without executing the command until Enter is pressed.
- Improve startup and reload diagnostics with affected contacts, guardian-link repair details and recovery steps specific to the failure.
- Clarify manual file editing, recovery, filtering, index behaviour and diagnostic-log retention in the guides; update examples, use cases, diagrams and acknowledgements.

### Fixed

- Corrected Developer Guide manual-test results for Unicode substring search, preserved significant repeated spaces in copyable tag, JSON, and phone examples, and distinguished startup recovery from reload outcomes (#262, #288).
- Split the UI UML documentation into a component overview and a detailed UI-parts diagram with a note documenting the relevant inheritance relationships while retaining standard UML notation.
- Reject unrecognised fields in the contact data file and preserve the original file through load recovery, preventing misspelled or incorrectly cased field names from silently discarding values (#292).
- Reject lookalike and invisible characters at command-syntax boundaries: non-ASCII digits are no longer accepted as indexes (so `delete １` no longer deletes contact 1), a Kelvin-sign `linK` no longer runs `link`, `ſtudent`/`guardıan` are no longer accepted as roles, and every Unicode or zero-width space in command words and indexes now shows the unusual-space hint (#142).
- Reject addresses containing unpaired surrogates, which were silently saved as `?`, or embedded control characters other than tabs, and stop treating combining marks alone as visible address content (#142).
- Reject hidden control characters before command trimming and preserve them through field and role validation, preventing control-padded indexes from executing and control-only optional fields from being cleared; ordinary whitespace and intentionally empty fields retain their existing behavior (#142).
- Report when `edit` leaves all contact values unchanged instead of claiming the contact was edited, including repeated values, clearing already-empty fields, and reordered identical tag sets; unchanged edits continue to skip saving.
- Split the dense Developer Guide parser diagram into focused dispatch and utility views, moving explanatory notes and command inheritance into prose (#225).
- Report a data file with an extra JSON value after its closing brace as unreadable JSON rather than a file-system error (#267, #269).
- Name the affected contact, when its name is valid, alongside its position in data-file load errors such as an invalid email, and tell users they can enter `reload` instead of only restarting after recovering the data file (#277).
- Allowed a student and guardian to share the same normalized name and phone number while retaining duplicate rejection within each role (#271).
- Suggest the selected contact's exact stored tag spellings when an absent `untag` argument differs only in case, while retaining case-sensitive tag identity and removal (#217).
- Explain role immutability and direct users to `link`/`unlink` when `edit` contains unsupported role or guardian-link fields, showing both explanations when applicable (#218).
- Trim surrounding ASCII spaces from tags loaded from JSON, making previously padded tags addressable through `list`, `tag`, and `untag` while preserving significant internal spaces; tags that differ only by surrounding spaces are merged into one (#185).
- Made equal `Index` values produce the same hash code, including equivalent one-based and zero-based indexes (#212).
- Recovered from a `null` `guiSettings` value in `preferences.json` by restoring and saving default window settings instead of failing during startup (#192).
- Show the fatal startup error dialog when a packaged UI resource is missing or malformed, instead of raising a secondary null-window exception.
- Rejected whitespace-, control-, or format-only and line-breaking addresses consistently when entered through commands or loaded from JSON.
- Normalized phone spaces, hyphens, slashes, numeric-group parentheses, annotations, and equivalent extension markers during duplicate detection while retaining a leading `+` and extension digits.
- Reported every missing mandatory `add` field in role, name, phone order, and showed the ordinary example before quoted-prefix guidance.
- Made duplicate name identity consistently compare `Groß` with `GROẞ`, while preserving the existing name and phone normalization rules and original display spelling.
- Made literal name-substring search use the same locale-independent Unicode comparison form for stored names and keywords, so composed/decomposed accents and the demonstrated sharp-S cases match consistently without applying duplicate identity's space normalization.
- Focused the command box after startup and after dismissing a startup notice so typing is immediately available without accidentally activating Help.
- Allowed contact cards to grow and shrink with wrapped tag rows, preventing tags from obscuring contact details or neighbouring cards at narrow widths.
- Split the dense delete sequence diagram into focused parsing and execution diagrams that remain readable at normal browser zoom.
- Reject unknown space-separated ASCII-letter prefixes in `add`, `edit`, `list`, `tag` and `untag` rather than silently absorbing mistyped prefixes into field values; intentional literal text must be quoted (#193). Stored values, field validation, tag identity and whitespace trimming are unchanged.
- Scroll the contact list to a newly added contact until it is visible, then stop retrying so manual scrolling is not overridden.
- Use locale-independent, case-insensitive alphabetical ordering for tags in contact cards and detailed views.
- Render detailed-view tags with brackets and escape literal backslashes and square brackets.
- Escape literal backslashes and square brackets in tags shown by `add`, `edit` and `delete` confirmations, as `view` does, so a single tag such as `first][second` is no longer indistinguishable from the two tags `first` and `second` (#268).
- Keep the current screen visible when a command fails instead of forcing a return to the contact list.
- Give bare and invalid `clear` confirmations distinct guidance without changing `clear confirm` behaviour.
- Reject extra arguments to `help` and `exit`, consistently with `reload`, rather than opening Help or exiting unexpectedly.
- Report raw line-break code points and correction guidance when input reaches the parser without command-box conversion, and diagnose non-breaking/full-width spaces in command words and indexes without normalising those characters in field values.
- Update the UG, DG manual tests and offline Help to describe the development build's command-input rules rather than the old permissive behaviour.
- Reject JSON files containing trailing content or duplicate property names so that ambiguous data is preserved through the existing malformed-file recovery workflow instead of being silently discarded on the next save.
- Simplified startup warnings: the main message shows the problem and whether a backup was made, with recovery steps and file locations under **Recovery details**.
- Continue startup with the unchanged legacy `addressbook.json` when permissions prevent migration to `tuitionbook.json`, allowing migration to be retried on a later launch.
- Show all repaired guardian links in a scrollable startup summary while retaining the existing in-memory repair and next-save behaviour.
- Decode JSON files strictly as UTF-8 so UTF-8 BOMs are accepted while invalid UTF-8 and other encodings are rejected and preserved unchanged through recovery.
- Report permission, encoding/JSON, invalid contact-value, and other file-system load failures with appropriate startup and reload recovery guidance.
- Made the UG's index-based examples runnable by adding shared sample contacts with their expected list, rebasing the command summary and the `view`, `edit`, `tag`, `untag`, `link`, `unlink`, `find`, `delete` and stable-index examples onto them, and marking which examples run in order (#195).
- Link the UG version notice directly to the v1.3 and v1.4 User Guides saved with those releases, whose release pages contain no guide link (#223).
- Show `PersonListPanel`'s retained `Person` references as an association in the UI class diagram, and identify the architecture sequence diagram as a simplified view of the detailed `delete` sequence (#177).

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
