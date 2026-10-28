# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog.

- Keep a Changelog: https://keepachangelog.com/en/1.1.0/

## [v1.5] - 2026-10-28

### Added

- Enforce one running TuitionBook instance per application home using an operating-system file lock, while allowing copies in separate folders to run independently (#256).
- Show the active query and persistent displayed, matching and retained contact counts above the contact list, including after detail views, reload and save-failure rollback (#188).
- Support quoted addresses in `add` and `edit`, protecting complete addresses containing prefix-like text and using the same quote/backslash escapes as names and tags (#201).
- Detailed guardian-link repair diagnostics in the startup warning, including every affected contact and repair reason.

### Changed

- Accept the full unquoted ASCII email local-part character set, including punctuation at either end, while rejecting leading, trailing and consecutive periods, spaces and double quotes; retain the email length limits and domain rules, and clarify how emails containing field-like text such as `t/` are entered.
- Consolidated the Developer Guide's AI acknowledgement to identify Codex and Claude, their extensive use in TuitionBook-specific development, and human oversight through team code review (#149).
- Added assertions that execution-dependent save decisions are queried only after a command execution attempt and required every command to declare whether it can mutate contact data for pre-execution rollback checks.
- Clarify separate workflows for routine manual file edits with TuitionBook closed and load recovery using `reload` while it stays open, including backup precautions, replacement of in-memory contacts, and filter reset; align the DG manual tests (#196).
- Clarify that rollback snapshot creation performs quadratic pairwise duplicate validation alongside linear reference copying, including the cost for commands that ultimately make no change (#224).
- Clarified that `clear confirm` removes all contacts but not separately stored diagnostic logs, which may retain earlier command information and should be reviewed before sharing; no secure-erasure guarantee is made.
- Align the UG and DG descriptions of indexes, unchanged commands, reload and save-failure recovery with exact working-set membership, order and query; share the full-list refresh command across model, commands and UI.
- Clarify that `list` locates hidden additions while rerunning the originating query only refreshes its working set; include ordered displayed UUIDs, query predicates and refresh text when comparing models.
- Accept command words in any letter case without changing field prefixes, values or the lowercase `confirm` keyword.
- Acknowledged in the Developer Guide the bug reports from team T16-1's TAB project that were adapted, with that team's approval, as bug-finding test scenarios for TuitionBook.
- Documented save-failure rollback in the Delete use case, including restoration of the contact, guardian links, and active filter.
- Added a Developer Guide use case for reloading contact data, including missing-file, repaired-link, successful-backup, and blocked-saving outcomes.
- Reload now reports every repaired guardian link with the affected contact's UUID, guardian ID when present, and reason; load failures include cause-specific recovery guidance.
- Expanded the Developer Guide use cases for combined role-and-tag filtering and confirmed clearing, including zero-match, invalid-request, hidden-contact, and save-failure outcomes.
- Expanded the documented contact-field validation subset to support right curly apostrophes in names; phone groups separated by spaces, hyphens, or slashes, optional trailing phone annotations, and numeric extensions; apostrophes and punycode labels in email addresses; and explicitly single-line addresses.
- Clarified that names accept ordinary ASCII spaces (U+0020), rather than every Unicode whitespace character.
- Shortened invalid name, phone, email, address and tag errors into user-facing messages that report the specific rule broken, and removed quoted-value guidance from the `add`, `edit`, `list`, `tag` and `untag` usage messages (#269).
- Reworded unknown-field, repeated-field, quote, unusual-space, line-break, `g/` in `add`, `find` usage and save-failure messages in plain language without character codes; save failures now explain that the command was undone instead of showing raw system errors (#269).
- Rewrote the User Guide in plain language for tutors, replacing character codes and technical terms with everyday wording, and added a tip explaining when to wrap names, addresses and tags in double quotes (#269).
- Rewrote the Help window's command summary in plain language, with a short guide to reading the command formats and a section on when to use double quotes (#269).
- Data-file load errors now name the affected contact by its position in the file, use the file's own key names such as `"name"` and `"tags"`, and describe unreadable JSON in plain language (#269).
- Pasted tabs and line separators in the command box are converted to spaces so that adjacent words remain separate; pasted text is not executed until Enter is pressed.
- Preserve an ordered contact working set across edit, tag, untag, link and unlink; retain and mark contacts that stop matching with canonical refresh commands, append matching additions, explain hidden additions, and warn when deletion compacts indexes. Explicit queries refresh membership; reload, clear and restart reset it. Save failures restore exact membership, order and query (#187).
- Clarified the supported `guardianId` JSON value types and the difference between link repair and whole-file recovery.
- Split the unreadable-data startup warning into separate paragraphs for the problem, the preserved or unpreserved file location, and the recovery steps, so long file paths no longer bury the guidance (#286).

### Fixed

- Reject email addresses exceeding 254 characters overall, 64 before the `@`, or 63 in any domain part, with specific errors shared by add, edit and JSON loading; existing invalid files follow the current recovery flow without migration.
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
