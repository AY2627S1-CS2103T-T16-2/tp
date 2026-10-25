---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# TuitionBook User Guide

TuitionBook helps **private one-to-one home tutors** manage students, guardians and their contact details in one place. Link students to guardians to find the right person to call, keep home addresses for visits, and use tags to organise contacts by level, subject or status. Typed commands are the primary way to work with the desktop app; your contacts are stored locally.

This guide helps English-speaking tutors set up TuitionBook, record contacts and find the right person to call. You do not need programming knowledge. The interface is in English; contact names can contain letters from other languages, subject to the rules under each command.

**Using this guide:** New to TuitionBook? Start with the [Quick start](#quick-start) to set up and launch the app, then work through [Features](#features) for each command's rules and examples. The [Command summary](#command-summary) below is a cheat sheet for users who already know the commands. If something goes wrong, use [Troubleshooting](#troubleshooting). The [Glossary](#glossary) explains terms such as *role*, *guardian link*, and *index*.

**Version:** This guide describes the v1.4 source. For an older downloaded JAR, use the guide linked from its [release page](https://github.com/AY2627S1-CS2103T-T16-2/tp/releases); commands added in v1.4 may not be available in older releases.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add r/ROLE n/NAME p/PHONE [e/EMAIL] [a/ADDRESS] [t/TAG]...` <br> e.g., `add r/student n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague`
**Clear**  | `clear confirm`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...`<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Exit**   | `exit`
**Reload** | `reload`
**Find**   | `find KEYWORD [MORE_KEYWORDS]...` (name substrings)<br> e.g., `find rac bob`
**Help**   | `help`
**Link**   | `link STUDENT_INDEX GUARDIAN_INDEX` <br> e.g., `link 1 2`
**List**   | `list [r/ROLE] [t/TAG]...`<br> e.g., `list r/student t/ACTIVE t/math`
**Tag**    | `tag INDEX t/TAG [t/TAG]...`<br> e.g., `tag 1 t/Math t/Sec 4`
**Untag**  | `untag INDEX t/TAG [t/TAG]...`<br> e.g., `untag 1 t/Math`
**Unlink** | `unlink STUDENT_INDEX`<br> e.g., `unlink 1`
**View**   | `view INDEX [v/]`<br> e.g., `view 1 v/`

--------------------------------------------------------------------------------------------------------------------

## Quick start

Before you begin, install Java `25` or later and choose a folder where you can create files. TuitionBook saves its data beside the application JAR. To try the sample contacts without mixing them with real contacts, use a new, empty folder. Run only one copy of TuitionBook against that folder.

1. Check Java by opening a terminal (Terminal on macOS/Linux, or PowerShell on Windows) and entering `java -version`.<br>
   **Expected:** The version starts with `25` or a later number. If Java is missing or older, follow the [Java installation guide](https://se-education.org/guides/tutorials/javaInstallation.html) before continuing. **Mac users:** Follow the [macOS Java instructions](https://se-education.org/guides/tutorials/javaInstallationMac.html).
1. Download the `.jar` file for the version you want from [GitHub Releases](https://github.com/AY2627S1-CS2103T-T16-2/tp/releases), then copy it into your chosen folder.
1. In the terminal, run one of the commands below. Replace the example path with the actual path and filename of your downloaded JAR; keep the surrounding quotes.

   * **macOS/Linux:** `java -jar "/path/to/tuitionbook.jar"`
   * **Windows:** `java -jar "C:\path\to\tuitionbook.jar"`

   **Expected:** The TuitionBook window opens. In a fresh folder, the contact list is empty and a dialog explains that no data file was found. Dismiss the dialog. If the window does not open, see [Troubleshooting](#troubleshooting).
1. Click the command box labelled `Enter command here...`, type `add r/student n/John Doe p/98765432`, and press <kbd>Enter</kbd>.<br>
   **Expected:** John Doe appears as a student with a displayed index. The result area confirms the addition. A guardian link, email and home address are optional.
1. Enter `find John` in the same command box.<br>
   **Expected:** John Doe appears in the results. In this fresh sample list, the result area reports `1 person(s) listed!`.
1. Enter `list`.<br>
   **Expected:** All your contacts appear again. The index beside each contact is the number to use in commands such as `view 1`.
1. Enter `help` to see the command summary. Close the Help window when you are done, then enter `exit` in the main window.<br>
   **Expected:** TuitionBook closes. Successful contact changes have already been saved; there is no separate Save command.
1. Run the same terminal launch command again.<br>
   **Expected:** John Doe is still present. You can now use the [Features](#features) reference or the [status-tag workflow](#organising-levels-subjects-and-status-with-tags).

The command box is where you enter instructions; the result area reports success or errors; the contact list shows the contacts selected by your current filter. The [view command](#viewing-a-contact-view) temporarily replaces the list with full contact details.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]...` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters with a prefix, such as `n/NAME` and `p/PHONE`, can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE`, `p/PHONE n/NAME` is also acceptable. Positional parameters have no prefix and must appear in the order shown: `edit 2 n/James Lee` puts its index first; `link 1 2` puts the student index before the guardian index.

* The `help` and `exit` commands ignore extraneous parameters.<br>
  For example, `help 123` is interpreted as `help`. `reload` accepts no arguments, and `clear` requires exactly `confirm`; `reload 123` and `clear confirm extra` report errors instead of running.

* Enter one command at a time and press <kbd>Enter</kbd> to run it. The command box is single-line; when you paste text, tabs and line separators become spaces so that adjacent words are not joined. Pasting text does not run a command automatically.
</box>

### Viewing help: `help`

Opens the Help window, which contains a scrollable summary of every command and its syntax.

Format: `help`

* You can also open or focus the same Help window by clicking the one-click `Help` control at the top of the main window, or by pressing <kbd>F1</kbd> even while the command box has focus.
* Use `Open User Guide` to open this User Guide in your system's default browser.
* If TuitionBook cannot open a browser, the Help window displays fallback guidance and remains open. Use `Copy URL` to copy the User Guide address and open it manually.
* An internet connection is needed to open the linked page itself.


### Adding a contact: `add`

Adds a student or guardian contact to TuitionBook.

Format: `add r/ROLE n/NAME p/PHONE [e/EMAIL] [a/ADDRESS] [t/TAG]...`

* `ROLE`, `NAME`, and `PHONE` are mandatory.
* `ROLE` must be either `student` or `guardian`, case-insensitively.
* `NAME` must start with a Unicode letter or number. It may contain Unicode letters and numbers, Unicode combining marks, spaces, periods (`.`), straight apostrophes (`'`), forward slashes (`/`), and hyphens (`-`). Surrounding whitespace is ignored, while the accepted name's capitalization, punctuation, and Unicode representation are preserved.
* `NAME` can contain at most 100 characters.
* In `add` and `edit`, wrap the entire name in straight double quotes when it contains a field prefix: `n/"Rina a/p Kaur"`. The quotes are not stored and do not count towards the name length. Ordinary names still work without quotes.
* **Unquoted names still use normal field parsing:** `n/Rina a/p Kaur` is interpreted as name `Rina` and address `p Kaur`, not as the full name. Use `n/"Rina a/p Kaur"` to preserve it.
* `PHONE` must contain at least three digits. It may start with `+`, and a single space or hyphen may appear between digits. These formatting characters are preserved.
* `EMAIL` and `ADDRESS` are optional. If supplied, `ADDRESS` must not be blank and can contain at most 200 characters. For example, `😀` counts as one character. Surrounding whitespace is ignored; the address is otherwise preserved as entered.
* Each `TAG` must be non-blank and may contain ASCII letters, digits, spaces, and printable punctuation, such as `Sec 4`, `Math--Science`, `C++`, or `Mrs. Lim's`. Leading, trailing, and repeated hyphens are allowed. Non-ASCII characters and internal control characters (such as tabs or newlines) are not accepted. Surrounding whitespace is trimmed; internal spaces, punctuation, spelling, and capitalization are preserved.
* **Use consistent spacing in tags.** `Sec 4` (one internal space) and `Sec  4` (two internal spaces) are different tags, even though they may look similar. `list t/Sec 4` will not match a contact whose only tag is `Sec  4`. This is intentional: only surrounding whitespace is trimmed, and repeated internal spaces are not collapsed. Use the same internal spacing when adding, editing, and filtering tags.
* Tags do not need quotes merely to contain spaces. Wrap the entire tag in straight double quotes when it contains text that resembles a field prefix recognised by the command: `t/"meet at a/c room"`. The quotes are not stored. Without the quotes, `a/c room` is parsed as an address in `add` and `edit`. In `list`, quoting similarly protects embedded `r/` and `t/` text.
* For a quoted name or tag, after the closing quote, end the command or enter a space followed by another field prefix. Missing closing quotes and extra text after the closing quote are rejected.
* Inside a quoted value, use `\"` for a literal double quote and `\\` for a literal backslash. For example, `t/"\"Sec 4\""` stores the tag `"Sec 4"` including its literal quotes, while `t/"Sec 4"` stores `Sec 4`. Use `t/"C:\\Math\\"` for a tag ending in a backslash. A backslash before any other character is preserved (for example, `\M` stays `\M`); sequences such as `\n` do not insert newlines.
* Unquoted values keep their quotes and backslashes literally, unless the first non-whitespace character is `"`, which starts quoted syntax.
* Decoded values still follow normal field validation: double quotes and backslashes are allowed in tags but not names.
* `g/` is not accepted by `add`; linking a guardian to a student is a separate operation.
* After a contact is added, TuitionBook shows the full contact list: any active name, role, or tag filter is cleared so that the new contact is visible.

<box type="tip" seamless>

**Tip:** A person can have any number of tags, including zero.

TuitionBook treats two contacts as duplicates only when both their normalized names and normalized phone numbers match. Name comparison is case-insensitive, ignores surrounding and repeated spaces, and treats canonically equivalent Unicode forms, such as é and e followed by a combining acute accent, as equal. Phone comparison removes spaces and hyphens while retaining a leading `+`. Country-code equivalence is not applied, so phone numbers that differ by a leading `+` are distinct.

A young student without their own phone number may reuse their guardian's phone number. If an email is supplied, the guardian's email may also be reused. The student and guardian remain distinct contacts because their names differ.
</box>

Examples:
* `add r/student n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add r/guardian n/Betsy Crowe p/1234567 t/family`
* `add r/student n/Alex Lim p/91234567 t/Sec-4 t/referred-by-Mrs-Lim`
* `add r/student n/"Rina a/p Kaur" p/91234567 a/123 Main Street`

### Listing contacts: `list`

Shows all contacts, or only contacts matching a specified role and/or all specified tags. `list` clears any active name, role, or tag filter.

Format: `list [r/ROLE] [t/TAG]...`

* `ROLE` is `student` or `guardian`, case-insensitively.
* Each `TAG` follows the same tag-name rules as [adding a contact](#adding-a-contact-add). Tag matching is exact but case-insensitive; all specified tags must be present.
* Spaces and punctuation are significant: `list t/SEC 4 t/math--science` requires both complete tags. `Sec  4` (two spaces) is different from `Sec 4`; `active!` does not match `inactive!` or `active`. In `list`, a space followed by `r/` or `t/` starts a new filter argument.
* To search for a tag containing `r/` or `t/` after a space, quote the complete tag, for example `list t/"study r/student t/weekly"`.
* Escaping works the same way in `add`, `edit`, `list`, `tag`, and `untag`: `list t/"\"Sec 4\""` matches the literal tag `"Sec 4"`, including one loaded from an existing JSON file.
* A filtered `list` replaces any active name, role, or tag filter. No matches is a successful result: `0 person(s) listed!`. Bare `list` restores all contacts in their original order.
* Repeating `t/` applies an additional tag condition. Repeating `r/` is rejected.
* `list r/student` shows only students; `list r/guardian` shows only guardians.
* Other trailing text is rejected.
* Before linking a student to a guardian after applying a filter, enter `list` to restore the full list so that both contacts can be selected.
* Each contact card displays a `STUDENT` or `GUARDIAN` role badge.
* Each contact card labels its phone number, email address, and home address with `Phone:`, `Email:`, and `Address:`.
* Student cards display a `Guardian:` line with the linked guardian's name and contact number, or `-` when no guardian is linked.
* Guardian cards display a `Students:` line with linked student names in contact-list order, or `-` when no students are linked. The names come from the complete contact list even when a filter hides the student cards.
* Missing email addresses and home addresses display as `Email: -` and `Address: -`.
* If a name, phone number, email address, relationship summary, or home address is wider than its card, the card keeps the start visible and uses an ellipsis. Hover over the shortened value to see it in full, or use `view INDEX v/` for a keyboard-accessible full-detail view. The role badge remains visible beside a long name.

### Organising levels, subjects and status with tags

Choose a consistent tag convention, for example `Sec4` for level, `AMath` for subject, and `Active` or `Inactive` for status. These are labels you choose, not dedicated fields or enforced categories. Tag filters ignore letter case but preserve internal spacing and punctuation.

To try this workflow in a fresh, empty contact list:

1. Enter `add r/student n/Alex Tan p/91234561 t/Active t/Sec4 t/AMath`.
1. Enter `list r/student t/Sec4 t/AMath` to show students with both the level and subject tags.
1. Enter `edit 1 t/Inactive t/Sec4 t/AMath` when Alex stops tuition. **Retype every tag you want to keep**: supplying `t/` values replaces the entire tag set. Include any additional tags on your real contact too.
1. Enter `list r/student t/Active`. Alex is excluded because the `Active` tag was replaced, not because `Inactive` automatically hides contacts.
1. Enter `list r/student t/Inactive`, then `edit 1 t/Active t/Sec4 t/AMath` to label Alex active again.
1. Enter `list` to show every contact regardless of status tags.

`Inactive` does not archive a contact or prevent editing, linking or deletion. Status labels do not change guardian relationships. There are no dedicated archive/restore commands. Use `tag INDEX t/TAG` and `untag INDEX t/TAG` to add or remove selected labels without replacing the other tags. These operations match stored tags case-sensitively, unlike `list` filters.

### Viewing a contact: `view`

Replaces the contact list with a contact's details and their recorded relationship. The next command other than `view` restores the contact list.

Format: `view INDEX [v/]`

* `INDEX` is the positive index shown in the current contact list. After `find` or `list r/ROLE`, it refers to the filtered list.
* Without `v/`, each linked contact appears as `NAME (CONTACT_NUMBER)`. A student view shows its linked guardian, while a guardian view lists its linked students in contact-list order.
* Add `v/` to show the linked guardian's or linked students' full contact details, including their role, phone, email, address, and tags.
* If a student has no guardian link, the result shows `No guardian linked.` If a guardian has no linked students, it shows `No students linked.` Related contacts are shown even when the current filter hides them.
* Missing optional email, address, or tags are shown as `-`. Tags appear in alphabetical order. `view` does not change contact data or the active filter.

To try the examples below from an empty contact list, enter these commands first:

1. `add r/student n/Alex Yeoh p/87438807`
1. `add r/guardian n/Bernice Yu p/99272758`
1. `link 1 2`

Examples using the contacts created above (enter `list` first if you have applied a filter):

* `view 1` replaces the contact list with Alex Yeoh's details and `Bernice Yu (99272758)` as his linked guardian.
* `view 1 v/` also shows Bernice Yu's full contact details.
* `view 2` replaces the contact list with Bernice Yu's details and lists `Alex Yeoh (87438807)` as a linked student.
* `view 2 v/` also shows Alex Yeoh's full contact details.
* `list r/student` followed by `view 1 v/` still shows Bernice Yu's details, although guardians are hidden from the list.

<span id="editing-a-person-edit"></span>

### Editing a contact: `edit`

Edits an existing contact in TuitionBook.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...`

Names support the same optional double quotes as `add`. For example, `edit 1 n/"Rina a/p Kaur"` changes the complete name without interpreting `a/` as an address prefix.

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* To remove an existing email or address, use `e/` or `a/` with no value respectively.
* Existing values will be updated to the input values.
* `edit` cannot change a person's role or guardian link. Use `link` or `unlink` to change a student's guardian link. The role specified with `add` applies only to the new contact.
* `NAME` must start with a Unicode letter or number. It may contain Unicode letters and numbers, Unicode combining marks, spaces, periods (`.`), straight apostrophes (`'`), forward slashes (`/`), and hyphens (`-`). Surrounding whitespace is ignored, while the accepted name's capitalization, punctuation, and Unicode representation are preserved.
* `NAME` can contain at most 100 characters.
* `PHONE` must contain at least three digits. It may start with `+`, and a single space or hyphen may appear between digits. These formatting characters are preserved.
* A supplied `ADDRESS` must not be blank and can contain at most 200 characters. For example, `😀` counts as one character. Surrounding whitespace is ignored; the address is otherwise preserved as entered.
* `TAG` follows the same space-and-punctuation and optional quoting rules described for [`add`](#adding-a-contact-add).
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.
* After a successful edit, TuitionBook shows the full contact list: any active name, role, or tag filter is cleared.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.

### Adding tags to a contact: `tag`

Adds one or more tags while keeping the contact's existing tags.

Format: `tag INDEX t/TAG [t/TAG]...`

* `INDEX` is a positive index in the currently displayed list, including after `find` or a filtered `list`. Both students and guardians can be tagged.
* Supply at least one non-blank tag. Tags follow the same character and spacing rules as `add`. Repeating a tag in the command has the same effect as supplying it once.
* Tag identity is case-sensitive: `Math` and `math` are separate stored tags, although `list` matches tags case-insensitively.
* Existing tags are retained. Success feedback lists only newly added tags, in sorted order and enclosed in double quotes; literal quotes and backslashes are escaped for clarity. Already-present tags are omitted from this list. If every requested tag is already present, the command reports this without saving or changing the displayed filter.
* When tags change, TuitionBook saves the contact and restores the full contact list. If saving fails, it restores the original contacts and filter.
* Only `t/` is recognised as a field prefix by `tag` and `untag`. A space followed by `t/` starts another tag unless it is inside a quoted value. Use the same quoting and escape rules as `add`, `edit`, and `list`: `tag 1 t/"study t/weekly"` adds one tag containing `t/weekly`; `untag 1 t/"study t/weekly"` removes it. Enclosing quotes are not stored, and surrounding whitespace is trimmed even inside quotes.
* Inside quoted tags, use `\"` for a literal double quote and `\\` for a literal backslash. For example, `tag 1 t/"\"Sec 4\""` adds the literal tag `"Sec 4"`, and `untag 1 t/"\"Sec 4\""` removes it. Missing closing quotes, extra text after the closing quote, and blank decoded tags are rejected before any tags change.

Example: If contact 1 is Sam Student and has `Math` and `Sec 4`, `tag 1 t/Science t/Math` adds `Science` while keeping both existing tags and reports `Added tags to Sam Student: "Science".` Continue using `add` to supply a new contact's complete initial tag set, or `edit` to replace an existing contact's entire tag set.

### Removing tags from a contact: `untag`

Removes only the specified tags while keeping the contact's other tags.

Format: `untag INDEX t/TAG [t/TAG]...`

* The index, tag validation, case sensitivity, prefix boundaries, saving, and filter rules are the same as for `tag`.
* Tag names must match exactly, including case and internal spacing. `untag 1 t/math` does not remove `Math`.
* Absent tags are skipped and listed in the feedback under `Tags not present:`. Names are shown in sorted order, enclosed in double quotes; literal quotes and backslashes are escaped in this display so tag boundaries are clear. If none of the requested tags is present, the command reports this without saving or changing the filter.
* A mixture of present and absent tags removes the present tags. Removing the final tag leaves the contact with no tags.
* A missing tag argument or an empty `t/` is rejected; use `edit INDEX t/` to clear the entire tag set.

Example: If contact 1 has `Math`, `Science`, and `Sec 4`, `untag 1 t/Math t/Science` leaves only `Sec 4`.

If contact 1 is Sam Student and has only `Math`, `untag 1 t/Math t/Science` removes `Math` and reports `Removed tags from Sam Student. Tags not present: "Science".`

### Linking a student to a guardian: `link`

Links a student contact to a guardian contact.

Format: `link STUDENT_INDEX GUARDIAN_INDEX`

* The first index must refer to a student and the second index must refer to a guardian.
* Both indexes refer to positive index numbers in the currently displayed contact list. Both contacts must be visible. If a filter hides either contact, enter `list` to restore the full list before using `link`.
* If the student already has a different guardian, the previous guardian is unlinked and the result identifies that previous guardian.
* Linking a student to the guardian they are already linked to is idempotent; TuitionBook reports that the link already exists and makes no change.
* After a link is created or replaced, TuitionBook shows the full contact list: any active name, role, or tag filter is cleared. Reporting an already-existing link makes no change, so the displayed list and its filter are preserved.

Example: `link 1 2` links the student at index 1 to the guardian at index 2.

### Removing a guardian link: `unlink`

Removes a student contact's guardian link without deleting either contact.

Format: `unlink STUDENT_INDEX`

* `STUDENT_INDEX` is the positive index shown in the currently displayed contact list. After `find` or a filtered `list`, it refers to the filtered list.
* The index must refer to a student.
* If the student has no linked guardian, TuitionBook reports this and makes no change. The displayed list and any active filter are preserved.
* After a guardian link is removed, TuitionBook shows the full contact list: any active name, role, or tag filter is cleared.

Example: `unlink 1` removes the guardian link of the student at index 1.

<span id="locating-persons-by-name-find"></span>

### Finding contacts by name: `find`

Finds contacts whose names contain any of the text you enter. Each piece of text can match part of a name (a substring).

Format: `find KEYWORD [MORE_KEYWORDS]...`

* Each keyword matches anywhere within a name, including the start, middle, or end of a name component. For example, `rac` matches `Grace Tan`.
* The search is Unicode-aware and case-insensitive; for example, `GRACE` matches `Grace Tan`, and `ışık` matches `Işık`.
* The search considers only contact names. Text found only in a phone number, email address, home address, tag, role, or linked contact's name does not match.
* Keywords are treated as literal text, so punctuation is not interpreted as a search pattern.
* Persons matching at least one keyword are returned (an `OR` search). A person is listed only once even if multiple keywords match their name.

Examples:
* `find gr`, `find rac`, `find ace`, and `find GRACE` each return `Grace Tan`.
* `find rac bob` returns contacts whose names contain either `rac` or `bob`, such as `Grace Tan` and `Bob Lim`.
* If your list contains `Alex Yeoh` and `David Li`, `find alex david` shows both. Any other contacts with `alex` or `david` anywhere in their names also appear. If no names match, the list is empty; use `list` to show every contact again.

<span id="deleting-a-person-delete"></span>

### Deleting a contact: `delete`

Deletes the specified contact from TuitionBook.

<box type="warning" seamless>

Deletion is immediate and cannot be undone. Check the contact name and its current displayed index before running the command. Keep a backup if you may need to restore the contact.
</box>

Format: `delete INDEX`

* Deletes the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, ...
* Deleting a guardian keeps their linked students, but clears those students' guardian links.
* Unlike `add`, `edit`, and `link`, `delete` does not clear an active name, role, or tag filter: the remaining contacts of the filtered view stay listed.

Examples:
* `list` followed by `delete 2` deletes the 2nd person in TuitionBook.
* `find Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.

### Clearing all entries: `clear confirm`

Clears all entries from TuitionBook, including contacts hidden by the current filter.

Format: `clear confirm`

<box type="warning" seamless>

This deletes every contact, including contacts hidden by a filter. There is no undo. Close the app and copy its data file to a safe location before reopening it if you need a backup.
</box>

* The confirmation keyword must be the exact lowercase word `confirm`.
* `clear`, `clear CONFIRM`, and any other trailing text are rejected without deleting contacts.

### Reloading contacts: `reload`

Format: `reload`

Reads the configured contact file again and replaces the current contact list, clearing any active filter. Use this after correcting a file that could not be loaded and backed up. A successful read enables saving again, but does not check write permissions or retry a rolled-back command. Re-enter that command afterwards. No arguments are accepted.

If the file is missing, the contact list becomes empty and saving is enabled. Reloading does not save anything. If reading fails, the current contacts and filter are retained. TuitionBook attempts to preserve the unreadable file in a backup: the error reports the backup path if successful (saving is then enabled), or states that saving remains blocked if preservation fails. Restore corrected data before reloading again if you want to recover contacts from the backup. Reload replaces rather than merges contacts; preserve any external file edits you need before using it.

### Exiting the program: `exit`

Exits the program without attempting to save contact data.

Format: `exit`

### Saving the data

**Run only one copy of TuitionBook against the same data file at a time.** There is no single-instance guard: another copy can overwrite the first copy's saves. Close the app before copying or replacing its data file.

TuitionBook attempts to save contact data after commands that change contacts or guardian links. Repeating an existing link, unlinking a student without a guardian, editing fields or tags to their existing values, or clearing an already-empty contact list does not attempt a save. A change to the displayed filter alone does not require saving. Read-only commands, including `help` and `exit`, do not save contact data. If saving fails, TuitionBook displays an error and restores the contacts and active filter to their state before the command. You do not need to save manually.
If TuitionBook cannot load an existing data file, it shows a startup warning with the recovery outcome. When it successfully moves the unreadable file to a backup, you can save new contacts. If it cannot create that backup, saving remains blocked until you correct or move the original file and run `reload` or restart the app.

### Editing the data file

**Close TuitionBook before editing or replacing its data file.** Make a backup first, edit the file, then reopen the app. An open instance keeps its own copy in memory and can overwrite external edits on the next save. `reload` is available for the recovery workflow described above, but it replaces contacts rather than merging changes.

TuitionBook stores its data, preferences, and logs relative to the folder containing the JAR, regardless of the folder from which it is launched. Contact data is saved automatically as a JSON file `[JAR file location]/data/tuitionbook.json`. Advanced users are welcome to update data directly by editing that data file. On first launch after upgrading from an earlier version, TuitionBook moves the existing `data/addressbook.json` to this path. If both files are present, it uses `tuitionbook.json` and leaves `addressbook.json` unchanged.

Each contact in the file has these relationship fields:

* `id`: the contact's unique identifier. It must be a valid UUID and must not be repeated in the file.
* `role`: either `STUDENT` or `GUARDIAN`.
* `guardianId`: optional. For a student, it is the `id` of a guardian contact; guardians must have no guardian link (the field is absent or `null`).

Do not change an existing `id` when editing the file. A current-format contact must include valid `id` and `role` fields. Contacts with the same normalized name and phone number are duplicates and make the file invalid, even when their `id` values differ.

Data files created before these relationship fields were introduced remain supported. A contact that has none of `id`, `role`, or `guardianId` is loaded as an unlinked student and receives a generated `id`. The updated fields are written when TuitionBook next saves the data file.

If a `guardianId` is malformed, refers to a missing contact, refers to a student, or links a student to itself, TuitionBook keeps the contact but removes that guardian link when loading the file. A startup warning gives the number of links repaired. Review the `guardianId` values in the data file before entering a contact-changing command; the next save removes invalid links. A guardian contact's own `guardianId` is also removed if present.

<box type="warning" seamless>

**Caution:** If your changes make the data file invalid, TuitionBook starts with an empty contact list and shows a warning at the next run. It moves the unreadable file unchanged to `tuitionbook.json.backup`. If that name already exists, it uses the next available name such as `tuitionbook.json.backup.1`; the warning names the exact backup path. The backup is kept unchanged, and TuitionBook can then save new contact data to `tuitionbook.json`.<br>
To restore the original data, first fix the JSON in the backup file, then rename it to `tuitionbook.json`, replacing the new file, and restart TuitionBook. Contacts added after recovery are stored in the new file and must be re-entered because TuitionBook cannot merge the two files automatically. If the backup cannot be created, TuitionBook leaves the original file in place and blocks saves; correct or move that file, then restart TuitionBook. Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause TuitionBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.

The 100-character name limit and 200-character address limit also apply to values edited directly in the data file. A file containing a longer value is invalid and follows the recovery behavior above: TuitionBook moves it unchanged to an unused sibling backup and allows new data to be saved; if that backup cannot be created, TuitionBook leaves the original file in place and blocks saves.
</box>

--------------------------------------------------------------------------------------------------------------------

## Glossary

* **JAR**: The `.jar` application file you download and run with Java.
* **Terminal**: The window where you enter the Java launch command. Once TuitionBook opens, enter contact-management commands in its command box instead.
* **Prefix**: A marker such as `n/` or `t/` that identifies the value following it.
* **Contact**: A record representing either a student or a guardian, with a name, phone number, and optional email address, home address, and tags.
* **Role**: Whether a contact is a `student` or a `guardian`. Entered in any casing; shown as a `STUDENT` or `GUARDIAN` badge on the contact card.
* **Guardian link**: A recorded association from a student to a guardian. A student has at most one guardian link; a guardian can be linked to multiple students.
* **Index**: A contact's position in the currently displayed list, starting from 1. Index-based commands such as `edit`, `delete`, and `view` use this displayed position, which changes when a filter is active.
* **Filter**: A condition that restricts the displayed list, set by `find` (name) or `list` (role and/or tags). Applying a new filter replaces the previous one; bare `list` clears it.
* **Tag**: An optional label you choose for a contact, such as `Sec4` or `AMath`, used for organising and filtering. Tags are not dedicated fields and are not validated against any category list.
* **Home folder**: The folder containing the TuitionBook JAR. Data, preferences, and logs are stored relative to this folder.
* **Data file**: The local JSON file (`data/tuitionbook.json`) that stores your contacts and guardian links between sessions.

--------------------------------------------------------------------------------------------------------------------

## Troubleshooting

Find the symptom below and try the suggested action. Error messages appear in the result area above the contact list. These checks do not require editing your contact data unless explicitly stated.

| Symptom or message | Likely cause | What to do |
|---|---|---|
| The terminal cannot find `java`, or reports an unsupported Java version | Java is missing, too old, or a different installation is being used | Run `java -version` in that terminal and follow the Java installation links in [Quick start](#quick-start). |
| `Unable to access jarfile` | The path or filename in the launch command is wrong | Check the downloaded file's location and name. Put the full path in straight double quotes. |
| `Unknown command.` | A misspelled command or wrong letter case | Use the lowercase command word from the [Command summary](#command-summary), without the backticks shown in this guide. |
| `Invalid command format!` | A required value is missing or a positional argument is misplaced | Read the usage shown with the error. Supply the required index first, then the relevant prefixed fields. For `link`, supply the student index followed by the guardian index. |
| `The contact index provided is invalid.` | That index is not in the currently displayed list | Enter `list`, find the intended contact's current index, and retry. Filters can change the displayed indexes. |
| No contacts appear after `find` or a filtered `list` | No contacts match the current filter | Enter `list` to show all contacts. `find` searches names only; tag filters must match the complete tag, including internal spaces. |
| A name or tag is split at text such as `a/` or `t/` | The text was interpreted as a field prefix | Quote the complete value, for example `n/"Rina a/p Kaur"` or `t/"study t/weekly"`. See [Adding a contact](#adding-a-contact-add). |
| A contact-changing command reports a save error | TuitionBook cannot write to its data location | Check write access and available disk space. The attempted change was rolled back. Correct the storage problem, then re-enter the command; it is not retried automatically. |
| Startup reports an unreadable file or blocked saving | The data could not be loaded, and a backup may also have failed | Follow the reported backup path and [data recovery instructions](#editing-the-data-file). Preserve the original and any new contacts before replacing files. Use `reload` or restart after correcting the file. |
| The Help window opens but the online guide does not | A browser could not open, or the page cannot be reached | Use the offline summary in Help. Try `Copy URL` in a browser when an internet connection is available. |

### Reporting a problem or unclear instruction

Check [Known issues](#known-issues) first. If you still need help, search the [project issue tracker](https://github.com/AY2627S1-CS2103T-T16-2/tp/issues) and report the problem there. Include your release version, operating system, exact command, expected result and actual message. For a documentation problem, link the section that was unclear. Use invented contact details in examples and hide real names, phone numbers, addresses and local paths in screenshots; do not upload your contact file.

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Close TuitionBook on both computers and back up their data files. Copy the application JAR and your contact data to the other computer, placing the JSON file at `data/tuitionbook.json` relative to the JAR. Replacing an existing file replaces those contacts; files are not merged. Open TuitionBook after copying.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. Close TuitionBook, rename `preferences.json` in the JAR folder to `preferences.json.old`, then reopen the app. This resets window preferences without changing `data/tuitionbook.json`.
2. **Unquoted name text resembling a parameter prefix** (e.g. `a/` in `Rina a/p Kaur`) is still read as a separate field. Wrap the entire name in straight double quotes: `n/"Rina a/p Kaur"`. Slashes without a recognised prefix, as in `Muhammad s/o Ali`, also work without quotes.
3. **Phone numbers are compared literally** (ignoring spaces and hyphens) when detecting duplicates: `+6591234567` and `91234567` are treated as different numbers. Enter phone numbers in a consistent format.
4. **Two contacts may share a name**: only a matching name *and* phone number counts as a duplicate. To tell namesakes apart at a glance, add a distinguishing suffix, e.g. `Tan Wei Ming - Sec 2`.
5. **A contact's role cannot be changed after it is added.** To correct a wrong role, delete the contact and add it again, then re-create any guardian links.
6. **`delete` and `clear confirm` cannot be undone.** The `clear` command requires the explicit lowercase confirmation keyword, but `delete` asks for no confirmation. Keep a separate backup of the data file before bulk changes so you can recover removed contacts.
7. **Each contact has exactly one role.** A person who is both a guardian and a student (e.g. an older sibling who pays for a younger one) needs two separate contacts with distinguishable names.
8. **If the data file is invalid or corrupted, TuitionBook starts with an empty contact list and a warning.** If it successfully moves the original bytes to a sibling backup named `tuitionbook.json.backup` (or the next available numbered name), the warning gives the exact backup path and TuitionBook allows new data to be saved. To recover the original data, fix the JSON in the backup, rename it to `tuitionbook.json` (replacing the new file), and restart; contacts added after recovery must be re-entered because the files cannot be merged automatically. If the backup fails, the original remains in place and saving is blocked; correct or move it, then restart.
