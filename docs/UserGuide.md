---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# TuitionBook User Guide

TuitionBook helps **private one-to-one home tutors** manage students, guardians and their contact details in one place. Link students to guardians to find the right person to call, keep home addresses for visits, and use tags to organise contacts by level, subject or status. You mainly use typed commands to work with the desktop app, which stores your contacts locally.

This guide helps English-speaking tutors set up TuitionBook, record contacts and find the right person to call. You do not need programming knowledge. Use TuitionBook with your operating system set to an English language setting, such as English (Singapore), English (United Kingdom) or English (United States). Contact names can contain letters from other languages, subject to the rules under each command.

**Using this guide:** New to TuitionBook? Start with the [Quick start](#quick-start) to set up and launch the app, then use the [Command reference](#command-reference) for each command's rules and examples. The [Task walkthroughs](#task-walkthroughs) combine commands for common jobs, while [Data and backups](#data-and-backups) covers saving, transfer and recovery. The [Command summary](#command-summary) below is a cheat sheet for users who already know the commands. If something goes wrong, use [Troubleshooting](#troubleshooting). The [Glossary](#glossary) explains terms such as *role*, *guardian link*, *index* and *JSON*.

**Version:** This guide describes v1.5, which has not been released yet. Some features described here, such as stable list numbering, stricter prefix checks and quoted addresses, are not in the v1.4 JAR. If you downloaded an older version, use the guide for that version instead: the [v1.4 User Guide](https://github.com/AY2627S1-CS2103T-T16-2/tp/blob/v1.4/docs/UserGuide.md) or the [v1.3 User Guide](https://github.com/AY2627S1-CS2103T-T16-2/tp/blob/v1.3/docs/UserGuide.md). These open on GitHub, so they look plainer than this page.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Command summary

Index examples refer to the [sample contacts](#sample-contacts-for-examples). Each example starts from the full sample list.

Action     | What it does | Format and example
-----------|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | Adds a student or guardian contact. | `add r/ROLE n/NAME p/PHONE [e/EMAIL] [a/ADDRESS] [t/TAG]...` <br> e.g., `add r/student n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague`
**Clear**  | Deletes **every contact**. | `clear confirm`
**Delete** | Deletes the contact at the selected index. | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | Changes details of the selected contact. | `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...`<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`
**Exit**   | Closes TuitionBook. | `exit`
**Reload** | Reloads contacts from the data file. | `reload`
**Find**   | Searches contact names. | `find KEYWORD [MORE_KEYWORDS]...`<br> e.g., `find rac bob`
**Help**   | Opens the command help window. | `help`
**Link**   | Links a student to a guardian. | `link STUDENT_INDEX GUARDIAN_INDEX` <br> e.g., `link 1 2`
**List**   | Shows all contacts or filters them by role and tags. | `list [r/ROLE] [t/TAG]...`<br> e.g., `list r/student t/active t/MATH`
**Tag**    | Adds one or more tags to a contact. | `tag INDEX t/TAG [t/TAG]...`<br> e.g., `tag 1 t/Math t/Sec 4`
**Untag**  | Removes one or more tags from a contact. | `untag INDEX t/TAG [t/TAG]...`<br> e.g., `untag 3 t/Math`
**Unlink** | Removes a student's guardian link. | `unlink STUDENT_INDEX`<br> e.g., `unlink 1`
**View**   | Shows full details for a contact. | `view INDEX [v/]`<br> e.g., `view 1 v/`

--------------------------------------------------------------------------------------------------------------------

## Quick start

Before you begin, install Java `25` or later and choose a folder where you can create files. TuitionBook saves its data beside the application JAR. To try the sample contacts without mixing them with real contacts, use a new, empty folder. TuitionBook enforces one running copy per folder.

1. Check Java by opening a terminal (Terminal on macOS/Linux, or PowerShell on Windows) and entering `java -version`.<br>
   **Expected:** The version starts with `25` or a later number. If Java is missing or older, follow the [Java installation guide](https://se-education.org/guides/tutorials/javaInstallation.html) before continuing. **Mac users:** Follow the [macOS Java instructions](https://se-education.org/guides/tutorials/javaInstallationMac.html).
1. Download the `.jar` file for the version you want from [GitHub Releases](https://github.com/AY2627S1-CS2103T-T16-2/tp/releases), then copy it into your chosen folder.
1. In the terminal, run one of the commands below. Replace the example path with the actual path and filename of your downloaded JAR, keeping the surrounding quotes.

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
   **Expected:** TuitionBook closes. Successful contact changes have already been saved, so there is no separate Save command.
1. Run the same terminal launch command again.<br>
   **Expected:** John Doe is still present. To try the examples in the [Command reference](#command-reference), add the [sample contacts](#sample-contacts-for-examples) next. You can also follow the [status-tag workflow](#organising-levels-subjects-and-status-with-tags).

Enter instructions in the command box and check the result area for success or error messages. The contact list shows the [contacts you are currently working with](#keeping-displayed-indexes-stable). The [view command](#viewing-a-contact-view) temporarily replaces the list with full contact details.

The screenshot below shows TuitionBook with the [sample contacts](#sample-contacts-for-examples). The command `view 1` is typed but has not been run yet.

<annotate src="images/ug-main-window.png" width="900" alt="TuitionBook showing the command box, result area and sample student and guardian contact cards with displayed indexes">
  <a-point x="88%" y="6.5%" label="1" color="#ffffff" opacity="1" content="Command box: type one command here and press Enter to run it." />
  <a-point x="88%" y="12%" label="2" color="#ffffff" opacity="1" content="Result area: check here for confirmation or help with an error." />
  <a-point x="88%" y="49%" label="3" color="#ffffff" opacity="1" content="Contact list: each card shows a student or guardian and their details." />
  <a-point x="15%" y="26%" label="4" color="#ffffff" opacity="1" content="Displayed indexes: use the number beside a contact in commands such as view 1." />
</annotate>

1. **Command box:** enter a command here.
1. **Result area:** read the confirmation or error message.
1. **Contact list:** see contact details, tags and guardian links.
1. **Displayed indexes:** use these numbers to choose contacts for commands such as `view`, `edit` and `delete`. They can change when the list changes.

--------------------------------------------------------------------------------------------------------------------

<a id="features"></a>

## Command reference

<box type="info" seamless>

**How to read command formats:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]...` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Follow each command's displayed order. Fields with a marker such as `n/` or `p/` can be supplied in either order, but unmarked values such as indexes stay in the position shown.

* Command words can be typed in any letter case: `list`, `LIST` and `LiSt` all work. Prefixes such as `n/` must be lowercase, and `clear confirm` must use the lowercase word `confirm`.

* Enter one command at a time and press <kbd>Enter</kbd> to run it. If you paste text containing tabs or line breaks, they become spaces. Pasting never runs a command by itself, so check the text before pressing <kbd>Enter</kbd>.

Move your pointer over a command block to show its copy button. Click it to copy the example, then paste it into TuitionBook. Run one command at a time. You can still select and copy text yourself.

For copied-text problems, unusual characters, field boundaries and escaping, see [Advanced command input](#advanced-command-input).
</box>

<box type="tip" seamless>

**Tip: use double quotes when a value contains text like `a/` or `s/`.**<br>
Names, addresses and tags sometimes contain text that looks like a prefix, such as `Rina a/p Kaur` or `c/o`. Wrap the **whole value** in straight double quotes so TuitionBook keeps it together:

* `n/"Muhammad s/o Ali"`
* `a/"20 Main Road, c/o Rina a/p Kaur"`
* `t/"study t/weekly"`

The quotes are not saved. You don't need quotes for slashes that don't follow a space, such as `t/Math/Science`, or just because a value contains spaces. See [Quoted values and escaping](#quoted-values-and-escaping) for the full rules.
</box>

### Sample contacts for examples

Index-based examples in this guide use the following sample contacts. Use a **new, empty folder used only for practice contacts**, not one containing real contacts. Starting from an empty contact list, enter these commands in order. If you completed the [Quick start](#quick-start), John Doe already exists, so skip the first command.

1. Add John Doe as a student:

   ```text
   add r/student n/John Doe p/98765432
   ```

1. Add Betsy Crowe as a guardian:

   ```text
   add r/guardian n/Betsy Crowe p/91234567 t/family
   ```

1. Add Grace Tan and her tags:

   ```text
   add r/student n/Grace Tan p/87654321 t/Active t/Math t/Sec 4
   ```

1. Add Bob Lim as a guardian:

   ```text
   add r/guardian n/Bob Lim p/96543210
   ```

1. Add Alex Yeoh as a student:

   ```text
   add r/student n/Alex Yeoh p/87438807 t/Active
   ```

1. Link John Doe to Betsy Crowe:

   ```text
   link 1 2
   ```


Then enter `list`. The list should show:

Index | Name | Role | Tags | Guardian link
------|------|------|------|--------------
1 | John Doe | Student | - | Betsy Crowe
2 | Betsy Crowe | Guardian | `family` | -
3 | Grace Tan | Student | `Active`, `Math`, `Sec 4` | -
4 | Bob Lim | Guardian | - | -
5 | Alex Yeoh | Student | `Active` | -

Unless a section says otherwise, each example starts from this full list. Enter numbered examples in order, as each step continues from the previous one. Examples that change contacts, such as `edit` or `delete`, change the results of later examples. To start again, enter `clear confirm` **only in the new, empty folder you chose for practice**, then re-enter the sample contacts.

### Viewing help: `help`

Opens the Help window, which contains a scrollable summary of every command and its syntax.

Format: `help`

* You can also open Help by clicking `Help` at the top of the main window, or by pressing <kbd>F1</kbd> at any time.
* Click `Open User Guide` to open this guide in your web browser. If the browser doesn't open, click `Copy URL` and paste the address into your browser yourself. You need an internet connection to view the online guide.


### Adding a contact: `add`

Adds a student or guardian contact to TuitionBook.

Format: `add r/ROLE n/NAME p/PHONE [e/EMAIL] [a/ADDRESS] [t/TAG]...`

Example:

```text
add r/student n/Sam Lee p/98760001 e/samlee@example.com t/Math
```

**Required and optional fields**

* `ROLE`, `NAME` and `PHONE` are required. Email, address and tags are optional.
* `ROLE` is `student` or `guardian` (any letter case, using ordinary English letters).
* `g/` cannot be used with `add`. Add the contact first, then use [`link`](#linking-a-student-to-a-guardian-link).
* If the new contact matches the current filter, it is added to the end of the displayed list. Otherwise it is saved but not shown, and the result tells you how to find it with `list`.

**Contact details at a glance**

* `NAME`: The contact's name, for example `Sam Lee`, `Mary-Jane O'Neil` or `陈俊杰`.
* `PHONE`: The number you use to contact them, for example `98760001`, `+65 9876 0001` or `6123 4567 (Office) ext. 89`.
* `EMAIL`: An optional email address, for example `samlee@example.com`.
* `ADDRESS`: An optional one-line home address, for example `123 Clementi Road, #01-01`.
* `TAG`: An optional label for organising contacts, for example `Math`, `Sec 4` or `Active`. A contact can have any number of tags, including none.

These examples illustrate common values. See [Detailed contact field rules](#detailed-contact-field-rules) for the full range of allowed values and any exceptions.

For values containing prefix-like text and for literal quotes or backslashes, see [Quoted values and escaping](#quoted-values-and-escaping).

<box type="tip" seamless>

TuitionBook stops you from adding the same person twice. Two contacts count as the same person when they have the same role, the same name **and** the same phone number. A student and a guardian can therefore share a name and phone number. When comparing:

* letter case and extra spaces in names are ignored, so `john doe` with additional spaces between the words still matches `John Doe`. Accented letters typed in different ways also match.
* spaces, hyphens, slashes, brackets and labels such as `(HP)` in phone numbers are ignored, and `x`, `ext` and `ext.` count as the same. However, a leading `+` matters: `+6591234567` and `6591234567` are different numbers.

These differences are ignored only for this check. TuitionBook still saves each name and phone number with the spelling, capital letters and formatting you entered.

A young student without their own phone number can use their guardian's phone number and email. They remain separate contacts because their roles differ, even if their names are the same.
</box>

Examples:

```text
add r/student n/Sam Lee p/98760001 e/samlee@example.com a/John street, block 123, #01-01
```

```text
add r/guardian n/Mary Lee p/1234567 t/family
```

```text
add r/student n/Alex Lim p/91234567 t/Sec-4 t/referred-by-Mrs-Lim
```

```text
add r/student n/"Rina a/p Kaur" p/91234567 a/123 Main Street
```


### Listing contacts: `list`

Shows all contacts, or only the contacts with a given role and/or tags.

Format: `list [r/ROLE] [t/TAG]...`

* `list` on its own shows every contact in its original order.
* Use `r/student` to show only students or `r/guardian` to show only guardians. You can give `r/` only once.
* Each `t/TAG` adds a condition: a contact is shown only if it has **all** the tags you list. Tags must match completely, but letter case doesn't matter, so `t/math` matches `Math`.
* Spaces and punctuation inside a tag must match exactly. For example, `Sec··4` does not match `Sec·4` (Using `·` as a placeholder for one space to make spaces visible here). You can copy the commands from the [add examples](#adding-a-contact-add). `active` also does not match `active!`.
* Stored tags keep their exact letter case, so one contact can have both `friends` and `friendS`. Filtering ignores that case (e.g. Searching `friends` will show all entries with the `friends` tags AND entries with the `friendS` tags).
* To filter by a tag that contains `r/` or `t/` after a space, use double quotes: `list t/"study t/weekly"`. See [Quoted values and escaping](#quoted-values-and-escaping).
* If nothing matches, the list is empty and the result shows `0 person(s) listed!`. Enter `list` to see everyone again.
* To link a student to a guardian after filtering, enter `list` first so that both contacts are shown.

**Reading a contact card**

* Each card shows a `STUDENT` or `GUARDIAN` badge, followed by the contact's phone, email and address. Missing details are shown as `-`.
* A student's card has a `Guardian:` line with the guardian's name and phone number. A guardian's card has a `Students:` line listing their linked students, even students hidden by the current filter.
* Tags are sorted alphabetically and wrap onto more rows when needed.
* If a value is too long for the card, it is shortened with `…`. Hover over it to see the full text, or use `view INDEX v/` to see every detail.

The bar above the cards explains the current list. **Matching** counts displayed contacts that still satisfy the query. **Retained** counts displayed contacts that stopped matching (usually after edits) but remain so their indexes do not suddenly change. Hidden contacts count in neither number. For a filtered view, **Shown = Matching + Retained**. See [Keeping displayed indexes stable](#keeping-displayed-indexes-stable) for why rows are retained and how to refresh them.

**Case variants and AND filtering**

Suppose Ana has only the tag `friends`, Ben has `friendS` and `Math`, and Cara has `friends`, `friendS` and `math`.

Query | Result | Why
------|--------|----
`list t/friends t/friendS` | Ana, Ben and Cara | Both conditions mean the same case-insensitive match. Repeating it does not require both stored spellings and does not create OR filtering.
`list t/friends t/Math` | Ben and Cara | These are different conditions, so normal **AND** filtering requires one friends-equivalent tag and one Math-equivalent tag.

Use [`tag`](#adding-tags-to-a-contact-tag) and [`untag`](#removing-tags-from-a-contact-untag) when the exact stored spelling matters.

### Viewing a contact: `view`

Shows a contact's full details together with their guardian or linked students.

Format: `view INDEX [v/]`

* `INDEX` is the number shown beside the contact in the current list.
* Without `v/`, linked contacts are shown as `NAME (PHONE)`. For a student, you see their guardian. For a guardian, you see their students.
* Add `v/` to show the linked contacts' full details too.
* If there is no link, the result shows `No guardian linked.` or `No students linked.` Linked contacts are shown even if the current filter hides them.
* Missing details are shown as `-`. Tags are sorted alphabetically and each is shown in square brackets, such as `[Math]`. A backslash is added before any `[`, `]` or `\` inside a tag so you can tell where each tag ends.
* `view` doesn't change any contacts or the current filter.

**Copying contact details**

To copy a phone number, email address or other contact details:

1. Enter `view INDEX`. Use `view INDEX v/` if you also need full details of linked contacts.
1. Click in the details pane and select the text you need.
1. Press <kbd>Ctrl</kbd>+<kbd>C</kbd> on Windows/Linux or <kbd>Cmd</kbd>+<kbd>C</kbd> on macOS to copy the text, then paste the text where you need it.

Take note that copying is only enabled in the details pane, while text on contact-list cards cannot be selected for copying.

Examples using the [sample contacts](#sample-contacts-for-examples):

* `view 1` replaces the contact list with John Doe's details and shows `Guardian: Betsy Crowe (91234567)`.
* `view 1 v/` shows John Doe's details together with his guardian Betsy Crowe's full contact details.
* `view 2` replaces the contact list with Betsy Crowe's details and lists `John Doe (98765432)` as a linked student.
* `view 2 v/` shows Betsy Crowe's details together with her linked student John Doe's full contact details.
* `list r/student` followed by `view 1 v/` still shows Betsy Crowe's details, although guardians are hidden from the list.

<span id="editing-a-person-edit"></span>

### Editing a contact: `edit`

Edits an existing contact in TuitionBook.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...`

* `INDEX` is the number shown beside the contact in the current list (1, 2, 3, …).
* Give at least one field to change. The new value replaces the old one, while fields you leave out stay the same.
* If all supplied values already match the contact, `edit` reports `No changes made: the specified values already match NAME` and does not save.
* All values follow the same rules as [`add`](#adding-a-contact-add), including using double quotes when a value contains text like `a/`, such as `edit 1 n/"Rina a/p Kaur"`.
* To remove an email or address, type `e/` or `a/` with nothing after it. `a/""` also removes the address.
* **Giving `t/` replaces all of the contact's tags.** Retype any tags you want to keep, or use [`tag`](#adding-tags-to-a-contact-tag) and [`untag`](#removing-tags-from-a-contact-untag) to change one tag at a time. To remove every tag, type `t/` with nothing after it.
* You cannot change a contact's role (`r/`) or guardian (`g/`) with `edit`. Use `link` and `unlink` to change guardian links. If you include `r/` or `g/`, nothing is changed and the result explains what to do instead.
* The edited contact keeps its place in the list, even if it no longer matches the current filter.

Examples using the [sample contacts](#sample-contacts-for-examples):

*  `edit 1 p/91110000 e/johndoe@example.com` changes John Doe's phone number to `91110000` and adds the email address `johndoe@example.com`.
*  `edit 2 n/Betsy Crower t/` renames Betsy Crowe to `Betsy Crower` and removes her `family` tag. John Doe stays linked to her.

### Adding tags to a contact: `tag`

Adds one or more tags while keeping the contact's existing tags.

Format: `tag INDEX t/TAG [t/TAG]...`

* `INDEX` is the number shown beside the contact in the current list. Students and guardians can both be tagged.
* Give at least one tag. Tags follow the same rules as [`add`](#adding-a-contact-add).
* `Math` and `math` are saved as two different tags, even though `list` treats them as the same.
* The result lists only the tags that were newly added. If the contact already has every tag you gave, nothing changes.
* Use double quotes for a tag containing text like `t/` or `p/` after a space: `tag 1 t/"study t/weekly"` adds one tag, `study t/weekly`. See [Quoted values and escaping](#quoted-values-and-escaping) for literal quotes, backslashes and feedback formatting.

Example using the [sample contacts](#sample-contacts-for-examples):

Grace Tan (3) has `Active`, `Math` and `Sec 4`. Enter:

```text
tag 3 t/Science t/Math
```

This adds `Science` while keeping her existing tags, and reports `Added tags to Grace Tan: "Science".` `Math` is not listed because it was already present. Use `add` instead to supply a new contact's complete initial tag set, or `edit` to replace an existing contact's entire tag set.

### Removing tags from a contact: `untag`

Removes only the specified tags while keeping the contact's other tags.

Format: `untag INDEX t/TAG [t/TAG]...`

* Works like [`tag`](#adding-tags-to-a-contact-tag): same index, tag rules and double quotes.
* Each tag must match exactly, including letter case and spacing.
* Tags the contact doesn't have are skipped and listed under `Tags not present:`. If none of them are present, nothing changes.
* To remove every tag at once, use `edit INDEX t/` instead.
* The contact keeps its place in the list, even if it no longer matches the current filter.

Examples continuing from the [`tag` example](#adding-tags-to-a-contact-tag), where Grace Tan (3) has `Active`, `Math`, `Science` and `Sec 4`. Enter them in order:

1. `untag 3 t/Math t/Science` removes both tags, leaving `Active` and `Sec 4`, and reports `Removed tags from Grace Tan.`
1. `untag 3 t/Sec 4 t/Math` removes `Sec 4`, leaving only `Active`, and reports `Removed tags from Grace Tan. Tags not present: "Math".`

### Linking a student to a guardian: `link`

Links a student contact to a guardian contact.

Format: `link STUDENT_INDEX GUARDIAN_INDEX`

* The first index must be a student and the second must be a guardian, both from the current list. If a filter hides either contact, enter `list` first.
* A student has at most one guardian. If the student already has a different guardian, that link is replaced and the result names the previous guardian.
* If the student is already linked to that guardian, nothing changes.
* The list order stays the same, and the `Guardian:` and `Students:` lines on the cards update straight away.

Examples using the [sample contacts](#sample-contacts-for-examples), entered in order:

1. `link 5 4` links Alex Yeoh to Bob Lim and reports `Linked student Alex Yeoh to guardian Bob Lim.`
1. `link 1 4` moves John Doe from Betsy Crowe to Bob Lim and reports `Linked student John Doe to guardian Bob Lim. Previous guardian unlinked: Betsy Crowe.`
1. Entering `link 1 4` again reports `Student John Doe is already linked to guardian Bob Lim.` and makes no change.

### Removing a guardian link: `unlink`

Removes a student contact's guardian link without deleting either contact.

Format: `unlink STUDENT_INDEX`

* `STUDENT_INDEX` is the number shown beside a student in the current list.
* If the student has no guardian, nothing changes.
* The list order stays the same, and the cards update straight away.

Example using the [sample contacts](#sample-contacts-for-examples):

```text
unlink 1
```

This removes John Doe's link to Betsy Crowe and reports `Unlinked student John Doe from guardian Betsy Crowe.` Both contacts remain. Running `unlink 1` again reports `Student John Doe has no linked guardian.`

<span id="locating-persons-by-name-find"></span>

### Finding contacts by name: `find`

Finds contacts whose names contain any of the keywords you type, even as part of a name.

Format: `find KEYWORD [MORE_KEYWORDS]...`

* A keyword can match any part of a name. For example, `rac` matches `Grace Tan`.
* Letter case doesn't matter: `GRACE` matches `Grace Tan`. Accented letters typed in different ways also match, and `STRASSE` matches `Straße`.
* A contact is shown if their name matches **any** of the keywords. Each contact appears only once.
* Only names are searched, not phone numbers, emails, addresses, tags or roles.
* Symbols in keywords are searched for as ordinary text.

Examples using the [sample contacts](#sample-contacts-for-examples):

* `find gr`, `find rac`, `find ace`, and `find GRACE` each list only Grace Tan.
* `find rac bob` lists Grace Tan and Bob Lim, whose names contain `rac` or `bob`.
* `find alex john` lists John Doe and Alex Yeoh. Any other contacts with `alex` or `john` anywhere in their names would also appear. If no names match, the list is empty. Enter `list` to show every contact again.

<span id="deleting-a-person-delete"></span>

### Deleting a contact: `delete`

Deletes the specified contact from TuitionBook.

<box type="warning" seamless>

Deletion is immediate and cannot be undone. Check the contact name and its current displayed index before running the command. Keep a backup if you may need to restore the contact.
</box>

Format: `delete INDEX`

* `INDEX` is the number shown beside the contact in the current list (1, 2, 3, …).
* Deleting a guardian keeps their students, but removes those students' guardian links.
* The contacts below the deleted one move up by one, so **check the list before your next command that uses an index**. Hidden contacts stay hidden.

Examples using the [sample contacts](#sample-contacts-for-examples). Each example starts from the full sample list:
* `list` followed by `delete 2` deletes Betsy Crowe, the 2nd contact in the full list. Because she was John Doe's guardian, the result also reports that John Doe is no longer linked to a guardian. Grace Tan, Bob Lim and Alex Yeoh move up to indexes 2 to 4.
* `find Betsy` followed by `delete 1` deletes Betsy Crowe, the 1st contact in the `find` results.

### Clearing all entries: `clear confirm`

Deletes all contacts, including any hidden by the current filter.

Format: `clear confirm`

<box type="warning" seamless>

This deletes every contact and cannot be undone. If you might need your contacts later, close the app and copy its data file somewhere safe first.
</box>

* You must type the lowercase word `confirm`. `clear` on its own, or with any other word, shows a reminder and deletes nothing.

`clear confirm` does not delete TuitionBook's log files (`tuitionbook.log.0` to `tuitionbook.log.4`, in the same folder as the JAR). These files may contain details from commands you entered earlier, so check them before sharing them with anyone. To remove them, close TuitionBook and delete those files. Normal file deletion does not securely erase data.

### Reloading contacts: `reload`

Format: `reload`

Reads your contacts from the data file again and shows the full list.

<box type="warning" seamless>

`reload` **replaces** the contacts in the app with the contents of the file. It does not combine them, so any contact that is only in the app will be lost.
</box>

For everyday edits to the data file, close TuitionBook, edit the file, then reopen it. Use this recovery path only when fixing an unreadable file while TuitionBook stays open:

1. Read and follow the specific warning, including its cause and file locations. Don't add or change contacts during recovery. Note any contacts created since the failure so you can re-enter them later.
1. Before replacing or moving any file, copy the current data file, if it exists, and any existing backup to a safe place. A backup exists only if TuitionBook says it preserved the unreadable file and shows its location.
1. If the unreadable file was preserved in a backup, fix a copy of that backup and put the corrected copy at the data-file location shown in the warning. If preservation failed, saving remains blocked: correct the original file at the shown data-file location, or move it aside and put a corrected copy there.
1. Once the corrected file is at the data-file location, enter `reload`. Check the result message and the full contact list before making changes. A successful reload enables saving. If reload fails, your currently loaded contacts stay in place and the result tells you whether the unreadable file was preserved or saving remains blocked.
1. Re-enter any noted contacts that are missing. TuitionBook replaces the currently loaded contacts with those from the corrected file. Contacts and files are not merged.

More about `reload`:

* If a command was undone because saving failed, `reload` does not redo it. Enter it again afterwards.
* If the data file doesn't exist, the contact list becomes empty.
* If the file still can't be read, your current contacts and list stay as they are. This differs from a startup load failure, which starts with an empty list. TuitionBook moves the unreadable file to a backup when it can and tells you where.
* If some guardian links in the file are invalid, the contacts still load but those links are removed. The result lists each affected contact and why its link was removed.

### Exiting the program: `exit`

Closes TuitionBook. Your changes are already saved, so there is nothing to save on exit.

Format: `exit`

--------------------------------------------------------------------------------------------------------------------

## Task walkthroughs

### Organising levels, subjects and status with tags

<box type="tip" seamless>

**Tip:** Choose a consistent way of naming tags, for example `Sec4` for level, `AMath` for subject, and `Active` or `Inactive` for status. You choose your own tags, and TuitionBook doesn't check them against any list. Tag filters ignore letter case, but spaces and punctuation must match.

</box>

This independent workflow uses its own contact, Alex Tan. To try it, start a new, empty folder used only for practice contacts, launch TuitionBook there, then enter these steps in order:

1. Enter `add r/student n/Alex Tan p/91234561 t/Active t/Sec4 t/AMath`.
1. Enter `list r/student t/Sec4 t/AMath` to show students with both the level and subject tags.
1. Enter `edit 1 t/Inactive t/Sec4 t/AMath` when Alex stops tuition. **Retype every tag you want to keep**: supplying `t/` values replaces the entire tag set. Include any additional tags on your real contact too.
1. Enter `list r/student t/Active`. Alex is excluded because the `Active` tag was replaced, not because `Inactive` automatically hides contacts.
1. Enter `list r/student t/Inactive`, then `edit 1 t/Active t/Sec4 t/AMath` to label Alex active again.
1. Enter `list` to show every contact regardless of status tags.

<box type="tip" seamless>

**Remember:** `Inactive` is only a label. It doesn't hide, archive or lock the contact, and it doesn't affect guardian links. To add or remove one label without retyping the others, use [`tag`](#adding-tags-to-a-contact-tag) and [`untag`](#removing-tags-from-a-contact-untag). Unlike `list`, these commands treat `Math` and `math` as different stored tags.

</box>

### Keeping displayed indexes stable

When you run `list` or `find`, the contacts it shows become your *working set*, and the numbers beside them are the indexes you use in commands. TuitionBook keeps these indexes steady so that the contact you meant doesn't move while you work:

* `edit`, `tag`, `untag`, `link` and `unlink` never reorder the list. If a change means a contact no longer fits your filter, it stays where it is, marked **No longer matches the query**, with a command you can run to refresh the list.
* A contact that is currently hidden stays hidden, even if a change makes it match, until you run `list` or `find` again. Hidden contacts are excluded from both **Matching** and **Retained**.
* A new contact that matches the filter is added to the end of the list. One that doesn't match is saved but not shown.
* To refresh membership, rerun the command shown after `Query:`, for example `list t/Active`. This removes retained rows that no longer match and adds now-matching hidden rows. `list` on its own shows everyone.

With a filter, the bar above the list can show `Shown: 2 of 5 contacts · Matching: 1 · Retained: 1`. **Shown = Matching + Retained**: Matching rows currently satisfy the query, while Retained rows stopped matching but remain displayed to preserve their indexes.

To verify this with the [sample contacts](#sample-contacts-for-examples), start from the full sample list and enter these steps in order:

1. `list t/Active`<br>
   **Expected:** Grace Tan (1) and Alex Yeoh (2) are listed. The counters show `Shown: 2 of 5 contacts · Matching: 2 · Retained: 0`.
1. `untag 1 t/Active`<br>
   **Expected:** Grace remains visible at index 1 with **No longer matches the query**, and Alex remains at index 2. Removing the required tag decreases Matching to 1 and increases Retained to 1, while Shown remains 2.
1. `list t/Active`<br>
   **Expected:** Rerunning the originating query refreshes membership. Grace is removed from this displayed list and only Alex is shown at index 1, with Matching 1 and Retained 0.

Deleting is the exception: contacts below the deleted one move up. Running `list` or `find`, `reload`, `clear confirm`, or restarting can also change indexes. Both `reload` and restarting show the full list. An index is a position in the current displayed list, not a permanent contact number.

--------------------------------------------------------------------------------------------------------------------

## Advanced command input

### Detailed contact field rules

These rules apply when adding or editing a contact and when TuitionBook reads contacts from its data file.

* `NAME`:
  * must start with a letter or number, and can be up to 100 characters long.
  * may contain letters from any language, numbers, accents, spaces, periods (`.`), apostrophes (`'` or `’`), forward slashes (`/`) and hyphens (`-`). Other symbols, such as `@`, `#` or double quotes, are not allowed.
  * is saved exactly as you type it, including capital letters and punctuation. Spaces at the start or end are ignored.
* `PHONE`:
  * must contain at least 3 digits (0-9) in the main number, not counting any label or extension, and may start with `+`.
  * can be split into groups of digits using spaces, a hyphen (`-`) or a slash (`/`), for example `9123 4567`, `9123-4567` or `+65/9123/4567`. A group can also be put in brackets, as in `+65 (9123) 4567`.
  * can end with a label in brackets after a space, such as `(HP)` or `(Office 2)`. A label contains English letters, digits and single spaces, with at least one letter.
  * can end with an extension written as `x123`, `ext 123` or `ext. 123`.
  * is displayed exactly as you type it. For example, `+65 (9123) 4567 (HP) ext. 89` is accepted.
* `EMAIL` must look like `name@example.com`:
  * The whole email can be up to 254 characters long, including the `@` and periods.
  * Before the `@`: up to 64 characters, using English letters, digits (0-9), periods (`.`), and these symbols: `` ! # $ % & ' * + - / = ? ^ _ ` { | } ~ ``. A period cannot be first or last, and two periods cannot appear in a row. The other allowed symbols can appear at the start or end. For example, `_alice+@example.com` is accepted. Spaces and double quotes (`"`) are not allowed.
  * Slashes inside an email do not start another field: `e/alice.t/user@example.com t/Math` stores email `alice.t/user@example.com` and tag `Math`. If an email starts with text such as `t/`, put it directly after `e/`: `e/t/user@example.com`. A space before `t/` starts a tag field, so `edit 1 e/ t/user@example.com` clears the email and sets the tag to `user@example.com`.
  * After the `@`: a domain such as `example.com` or `school.edu.sg`, made of English letters, digits (0-9) and hyphens, separated by periods. Each part can be up to 63 characters long, must start and end with a letter or digit, and the last part must be at least 2 characters long.
* `ADDRESS` must fit on one line, can be up to 200 characters long, and must contain at least one visible character. It can contain any letters and symbols. Spaces at the start or end are ignored.
  * Accent marks on their own do not count as visible characters.
  * If an address is rejected because it contains hidden control characters (other than tabs) or broken characters from copied text, retype it.
* Each `TAG`:
  * may contain English letters, digits (0-9), spaces, and punctuation and symbols from a standard English keyboard, for example `Sec 4`, `Math--Science`, `C++`, `$50/hr` or `Mrs. Lim's`. Letters with accents, other languages and other symbols (such as `—`) are not allowed.
  * cannot be blank. Spaces at the start or end are ignored.
  * keeps the exact spacing inside it. The commands below differ only in the space between `Sec` and `4`, with two spaces in the second command. They store different tags and do not match each other in `list`:

    ```text
    add r/student n/One Space p/90000001 t/Sec 4
    add r/student n/Two Spaces p/90000002 t/Sec  4
    ```

    As a visible-space illustration, the values are `Sec·4` and `Sec··4`, where each `·` represents one space and is not typed.

For values containing prefix-like text and for literal quotes or backslashes, see [Quoted values and escaping](#quoted-values-and-escaping).

### Field boundaries and copied text

* Command words, prefixes, roles and indexes must use ordinary English letters and the digits 0-9. Lookalike characters, such as a Cyrillic `а`, a full-width `ｌｉｓｔ` or a full-width index `１`, are not recognised.
* Type spaces with the spacebar. TuitionBook rejects special spaces in command words and indexes. These can look like ordinary spaces when copied from a website or PDF. Retype copied spaces or any command that produces a hidden-control-character error.
* `help`, `exit` and `reload` take nothing after the command word.
* In `add`, `edit`, `list`, `tag` and `untag`, a space followed by English letters and `/` starts a new field. An unknown marker such as `ee/` is rejected instead of being silently saved. Quote a whole value when this kind of text belongs inside it.

<a id="quoted-values-and-escaping"></a>

### Quoted values and escaping

Wrap a whole name, address or tag in straight double quotes when it contains prefix-like text after a space: `n/"Rina a/p Kaur"`, `a/"20 Road c/o Rina"` or `t/"meet at a/c room"`. This works in `add`, `edit`, `list`, `tag` and `untag`. The enclosing quotes are not saved. Names cannot contain literal quotes or backslashes.

Inside a quoted address or tag, `\"` means one literal double quote and `\\` means one literal backslash. Other backslash sequences are not decoded: for example, `\n` stores a backslash followed by `n`, not a line break. Unquoted values do not decode any escapes.

The feedback for `tag` and `untag` surrounds each tag with quotes and adds backslashes before literal quotes and backslashes so the stored value is unambiguous. The table contrasts all three forms:

What you type | Stored tag value | Escaped form in feedback
--------------|------------------|-------------------------
`tag 1 t/"\""` | one double quote: `"` | `"\""`
`tag 1 t/\"` | backslash then double quote: `\"` | `"\\\""`
`tag 1 t/"\\"` | one backslash: `\` | `"\\"`
`tag 1 t/\\` | two backslashes: `\\` | `"\\\\"`

After a closing quote, end the command or type a space followed by the next field. A value that does not start with `"` is unquoted and stores quotes and backslashes literally.

### Text matching details

`find` compares names as literal text without regard to letter case. It also treats equivalent composed and decomposed accents alike and handles the documented `Straße`/`STRASSE` case consistently. It does not apply duplicate detection's space or phone normalisation.

--------------------------------------------------------------------------------------------------------------------

## Data and backups

### Saving the data

TuitionBook saves automatically after every command that changes your contacts or guardian links. There is no Save command.

* If saving fails, the command is undone and an error explains what to check. Fix the problem, then enter the command again.
* If TuitionBook can't read your data file when it starts, it shows a warning. It usually moves the unreadable file to a backup and starts with an empty list, and you can keep working. If it can't make the backup, it won't save anything until you fix or move the file and then run `reload` or restart. See [Editing the data file](#editing-the-data-file).

Only one TuitionBook window can use a folder at a time. If you start it again from the same folder, the second copy shows an error and closes without touching your contacts. To run a separate copy, put the JAR in a different folder. Each folder has its own contacts and settings.

TuitionBook creates a file named `.tuitionbook.lock` in its folder to make this check. It's safe to leave this file after closing the app, but **don't delete it while TuitionBook is running**. This check only works between copies of this version of TuitionBook. Other programs can still edit the data file.

### Transferring contacts to another computer

Close TuitionBook on both computers and back up both data files. Copy the application JAR and `data/tuitionbook.json` together, keeping the data file in the `data` folder beside the JAR. Copying over an existing data file **replaces** its contacts. TuitionBook does not merge files. Open TuitionBook only after copying is complete.

### Editing the data file

Your contacts are saved in `data/tuitionbook.json`, inside the folder that contains the TuitionBook JAR. Settings and log files are kept in the same folder. Normally, manage contacts through TuitionBook's commands and do not edit the JSON file yourself. Manual editing is an advanced option for deliberate data-file work or recovery when the instructions below require it.

<box type="warning" seamless>

**Avoid manual editing unless it is necessary for recovery or a deliberate advanced task.** A small JSON mistake can make the whole file unreadable. If TuitionBook can't read the file, it starts with an empty contact list (see [If TuitionBook can't read the data file](#if-tuitionbook-can-t-read-the-data-file) below).
</box>

To edit the file:

1. Close TuitionBook. If it is open, it may overwrite your edits the next time it saves.
1. Copy the data file somewhere safe as a backup.
1. Edit the file and save it as UTF-8 text.
1. Reopen TuitionBook and check your contacts before making any changes.

If you are fixing the file after a problem and want to keep TuitionBook open, use [`reload`](#reloading-contacts-reload) instead.

**Rules for the data file**

* Each contact must follow the same rules as [`add`](#adding-a-contact-add), including the 100-character limit for names and 200-character limit for addresses.
* Each contact also has these fields, which TuitionBook manages for you:
  * `id`: a code that identifies the contact, in the format TuitionBook creates (known as a UUID), for example `123e4567-e89b-12d3-a456-426614174000`. Every contact needs one, except in files from older versions (see below). **Don't change it** or make up your own, such as `student-1`, and don't give two contacts the same `id`. Any of these makes the whole file unreadable.
  * `role`: `STUDENT` or `GUARDIAN`.
  * `guardianId`: for a student, the `id` of their guardian, written in double quotes. It can be left out or set to `null` if the student has no guardian. Any other kind of value, such as a list, makes the file unreadable.
* Two contacts with the same role, name and phone number count as duplicates, even if their `id`s differ, and make the file unreadable. A student and a guardian can share a name and phone number.
* Use only the root field `persons` and the contact fields `id`, `role`, `guardianId`, `name`, `phone`, `email`, `address` and `tags`. Field names use the exact letter case shown here. An unrecognised field such as `Email` makes the file unreadable, so a typing mistake cannot silently discard a value.
* The file must contain one set of contacts only. Extra text after the final `}`, or the same field name used twice in one contact, makes the file unreadable.
* Spaces at the start or end of a tag are removed when the file is loaded, so `" Math "` becomes `"Math"`. Spaces inside a tag are kept.
* Don't put a backslash before a space (for example `"\ Math"`). This is not valid JSON and makes the whole file unreadable.
* Files from older versions of TuitionBook still work. A contact without `id`, `role` and `guardianId` is loaded as a student with no guardian, and the missing fields are added the next time TuitionBook saves. If an older `data/addressbook.json` exists, TuitionBook moves it to `data/tuitionbook.json` the first time it starts. If it can't move the file (for example, because it isn't allowed to change that folder), it keeps using `data/addressbook.json` as your data file and tries again next time.

**Invalid guardian links**

If a student's `guardianId` doesn't point to a guardian in the file, TuitionBook still loads the student but removes that link. This happens when the `guardianId` is not a valid id, points to a missing contact or to another student, or points to the student themselves. A warning lists each affected contact and the reason. Check the file before making changes, because the next save removes those links permanently. Any `guardianId` on a guardian is also removed.

<a id="if-tuitionbook-can-t-read-the-data-file"></a>

**If TuitionBook can't read the data file**

TuitionBook starts with an empty contact list and shows the most specific warning it can. If the file can be parsed far enough to identify a particular contact, the warning gives its position and, when that contact's name is valid, its name. For example: `Contact 3 in the file ("Alex Yeoh"): Emails must be in the format name@example.com.` or `Contact 4 in the file: Missing "name" value.` Contacts are counted from the top of the file, starting from 1.

This diagnosis is best-effort. Broken or incomplete file structure may prevent TuitionBook from identifying a contact, name or exact location, and one problem may prevent it from discovering later problems in the same file. Use the reported cause as a starting point and check the rest of the file too. A missing `id` is not automatically an error because contacts saved by older supported versions may legitimately omit `id`, `role` and `guardianId`.

TuitionBook also moves the unreadable file, unchanged, to a backup in the same folder with `.backup` added to its name, for example `tuitionbook.json.backup` (or `.backup.1`, `.backup.2` and so on if that name is taken). **The warning shows the exact name and location**, so check it rather than assuming. You can then keep using TuitionBook, and new contacts are saved to a fresh data file.

To get your old contacts back:

1. Close TuitionBook. Copy both the data file and the `.backup` file somewhere safe.
1. Fix the problem described in the warning in a copy of the `.backup` file.
1. Replace the data file with your fixed copy (keeping the data file's original name), then reopen TuitionBook.
1. Re-enter any contacts you added after the problem started. TuitionBook cannot combine the two files for you.

If TuitionBook can't create the backup, it leaves the file where it is and won't save anything until you fix or move the file and restart (or use [`reload`](#reloading-contacts-reload)).

--------------------------------------------------------------------------------------------------------------------

## Glossary

* **JAR**: The `.jar` application file you download and run with Java.
* **Terminal**: The window where you enter the Java launch command. Once TuitionBook opens, enter contact-management commands in its command box instead.
* **Prefix**: A short marker such as `n/` or `t/` that tells TuitionBook what the value after it is (here, a name or a tag).
* **Contact**: A record representing either a student or a guardian, with a name, phone number, and optional email address, home address, and tags.
* **Role**: Whether a contact is a `student` or a `guardian`. You can enter the role in any letter case. It appears as a `STUDENT` or `GUARDIAN` badge on the contact card.
* **Guardian link**: A recorded association from a student to a guardian. A student can be linked to at most one guardian, while a guardian can be linked to multiple students.
* **Index**: The number shown beside a contact in the current list, starting from 1. Commands such as `edit`, `delete` and `view` use it to pick a contact. It can change after `delete`, `list`, `find`, `reload`, `clear confirm` or a restart.
* **Working set**: The contacts shown by your last `list` or `find`. Changing a contact keeps it in the displayed list, even if it no longer matches. Run the command again to refresh the list. See [Keeping displayed indexes stable](#keeping-displayed-indexes-stable).
* **Tag**: An optional label you choose for a contact, such as `Sec4` or `AMath`, used for organising and filtering.
* **Home folder**: The folder containing the TuitionBook JAR. Your data file, settings and log files are kept here.
* **Data file**: The local JSON file (`data/tuitionbook.json`) that stores your contacts and guardian links between sessions.
* **JSON**: A structured text format used by TuitionBook's data file. Its punctuation, quotes and field names must remain valid for TuitionBook to load it.
* **UTF-8**: The text encoding required for the data file. Choose UTF-8 when your editor asks how to save the file.

--------------------------------------------------------------------------------------------------------------------

## Troubleshooting

Find the symptom below and try the suggested action. Error messages appear in the result area above the contact list. These checks do not require editing your contact data unless explicitly stated.

| Symptom or message | Likely cause | What to do |
|---|---|---|
| The terminal cannot find `java`, or reports an unsupported Java version | Java is missing, too old, or a different installation is being used | Run `java -version` in that terminal and follow the Java installation links in [Quick start](#quick-start). |
| `Unable to access jarfile` | The path or filename in the launch command is wrong | Check the downloaded file's location and name. Put the full path in straight double quotes. |
| `TuitionBook is already running` | TuitionBook is already open from the same folder | Use or close the existing window. To run a separate copy, put the JAR in a different folder. Don't delete `.tuitionbook.lock` while TuitionBook is running. |
| `TuitionBook could not start safely` | TuitionBook could not create its `.tuitionbook.lock` file, for example because you can't save files in that folder | Check that you are allowed to create files in the JAR's folder, or move the JAR to a folder you own, then try again. |
| `Unknown command.` | A misspelled or unsupported command word | Type one of the command words in the [Command summary](#command-summary), such as `list` or `find`. The surrounding code styling in this guide is not part of what you type. Command-word letter case does not matter. |
| `Invalid command format!` | Something required is missing, or values are in the wrong order | Follow that command's usage and example shown with the error. Use no index for `add` and `find`. Start `edit` with the contact index. For `link`, enter the student index followed by the guardian index. |
| `Commands must be entered on a single line.` | The command contains a line break | Remove the line break or replace it with a space, then try again. |
| `Your command contains an unusual space character` | The command was copied from a website or document that uses special spaces | Retype the spaces around the command word and index using the spacebar, then try again. |
| `Missing required field(s): ...` | An `add` command is missing `r/ROLE`, `n/NAME` or `p/PHONE` | Add every field named in the message. |
| `The contact index provided is invalid.` | No contact has that number in the current list | Enter `list`, find the contact's number, and try again. |
| No contacts appear after `find` or `list` | No contacts match | Enter `list` to show everyone. `find` searches names only, and tag filters must match the whole tag, including spaces. |
| `Unknown field` | The command contains a prefix it doesn't recognise, such as `ee/` | Fix the typo. If the text is meant to be part of a name, address or tag (for example `s/o`), put the whole value in double quotes: `n/"Muhammad s/o Ali"`. Nothing was changed. |
| A name, address or tag is cut off at text such as `a/` or `t/` | TuitionBook read that text as the start of another field | Put the whole value in double quotes, for example `n/"Rina a/p Kaur"`, `a/"20 Road c/o Rina"` or `t/"study t/weekly"`. See [Quoted values and escaping](#quoted-values-and-escaping). |
| `Could not save your changes, so the command was undone.` | TuitionBook can't save to its data folder | Check that you can save files in the JAR's folder and that the disk isn't full, then enter the command again. If the message says saving is disabled, see [Editing the data file](#editing-the-data-file). |
| A warning says your contact data could not be loaded | The data file has a mistake or can't be opened | Follow the advice in the warning and the steps in [If TuitionBook can't read the data file](#if-tuitionbook-can-t-read-the-data-file). |
| The Help window opens but the online guide does not | A browser could not open, or the page cannot be reached | Use the offline summary in Help. Try `Copy URL` in a browser when an internet connection is available. |

### Reporting a problem or unclear instruction

Check [Limitations and known issues](#limitations-and-known-issues) first. If you still need help, search the [project issue tracker](https://github.com/AY2627S1-CS2103T-T16-2/tp/issues) and report the problem there. Include your release version, operating system, exact command, expected result and actual message. For a documentation problem, link the section that was unclear. Use invented contact details in examples and hide real names, phone numbers, addresses and local paths in screenshots. Keep your contact file private and do not upload it. Review diagnostic logs before sharing them because they may contain information entered during earlier commands.

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Follow [Transferring contacts to another computer](#transferring-contacts-to-another-computer). Remember that replacing the destination data file replaces its contacts. Files are not merged.

--------------------------------------------------------------------------------------------------------------------

<a id="known-issues"></a>

## Limitations and known issues

1. **Text that looks like a prefix is read as a new field** (e.g. `a/` in `Rina a/p Kaur`) unless you put the whole value in double quotes, for example `n/"Rina a/p Kaur"` or `n/"Muhammad s/o Ali"`. TuitionBook can't tell whether you meant it as part of the value.
2. **Country codes are not recognised in phone numbers.** When checking for duplicates, `+6591234567` and `91234567` are treated as different numbers.
3. **Two contacts may share a name**: they only count as the same person if their role, name *and* phone number all match. To tell apart two students (or two guardians) with the same name, add something to the name, e.g. `Tan Wei Ming - Sec 2`.
4. **A contact's role cannot be changed after it is added.** To correct a wrong role, delete the contact and add it again, then re-create any guardian links.
5. **`delete` and `clear confirm` cannot be undone.** The `clear` command requires the explicit lowercase confirmation keyword, but `delete` asks for no confirmation. Keep a separate backup of the data file before bulk changes so you can recover removed contacts.
6. **Each contact has exactly one role.** A person who is both a guardian and a student (e.g. an older sibling who pays for a younger one) needs two separate contacts, one for each role. The two contacts can have the same name and phone number.
7. **If the data file has a mistake, TuitionBook starts with an empty contact list and a warning.** When it can, TuitionBook moves the original file unchanged to a backup. If it cannot create that backup, saving stays blocked. See [If TuitionBook can't read the data file](#if-tuitionbook-can-t-read-the-data-file) for both recovery paths.
8. **Contact-list card text cannot be selected for copying.** Use the [details pane opened by `view INDEX`](#viewing-a-contact-view) to select and copy contact details instead.
