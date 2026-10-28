---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# TuitionBook Developer Guide

This guide is for developers maintaining TuitionBook and testers checking its behaviour. It explains the component boundaries, implemented features, scope decisions and manual test scenarios. It assumes familiarity with Java, object-oriented programming and basic UML diagrams. For everyday contact-management tasks, use the [User Guide](UserGuide.md).

**Reading paths:**

* New contributor: start with [Setting up](#setting-up-getting-started), then read [Architecture](#architecture) and the relevant [Implementation](#implementation) section.
* Feature or bug investigation: check [Product scope](#product-scope), the relevant command in the UG, and [Scope decisions](#scope-decisions-for-v1-4) before tracing parser, command, model and storage behaviour.
* Tester: use [Testing](Testing.md) for automated checks and [Manual testing](#appendix-instructions-for-manual-testing) for copyable scenarios and expected results.

**Version:** This guide describes the unreleased development source for v1.5. Its command-input changes, stable working sets, strict prefix checks and quoted addresses are not in the published v1.4 JAR. Requirements state intended guarantees, not measured test results. Historical proposals and deferred work are labelled separately from implemented behaviour. Consult the [release history](https://github.com/AY2627S1-CS2103T-T16-2/tp/releases) for published versions.

TuitionBook is scoped to English-language users and any `en-*` locale (for example, `en-SG`, `en-GB`, or `en-US`). Text input is encoded as UTF-8. Field-specific validation rules apply to names, addresses, phone numbers, email addresses, and tags.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5).
* The TuitionBook development team (CS2103T-T16-2) used [OpenAI Codex](https://openai.com/codex/) and [Anthropic Claude](https://claude.ai/) extensively for almost all TuitionBook-specific coding, including generating and revising implementation and test code. AI assistance also supported debugging, code review, and User Guide and Developer Guide updates. Team members provided human oversight by reviewing the resulting code.
* Some bug-finding test scenarios were adapted from bug reports in the public issue tracker of [TAB](https://github.com/AY2627S1-CS2103T-T16-1/tp), the team project of CS2103T team T16-1, with that team's approval to look through their issues. We rewrote the relevant scenarios using TuitionBook's own commands and data, and checked them against our implementation; no TAB code or documentation was copied for this testing. The reports that revealed or confirmed TuitionBook bugs are TAB issues [#129](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/129), [#145](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/145), [#172](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/172), [#183](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/183), [#187](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/187), [#188](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/188), [#193](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/193), [#196](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/196), [#198](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/198), [#286](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/286), [#312](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/312), [#313](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/313), [#315](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/315), [#317](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/317), [#322](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/322), [#330](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/330), [#341](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/341), [#348](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/348), [#373](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/373), [#374](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/374) and [#395](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/395).

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The architecture diagram above shows the main components. The following sections explain their responsibilities and the direction of calls between them.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-T16-2/tp/tree/master/src/main/java/seedu/tuitionbook/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-T16-2/tp/tree/master/src/main/java/seedu/tuitionbook/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

This is a simplified component-level view. It omits calls within a component's turn, such as resolving the displayed index, looking up linked students and snapshotting data for save-failure rollback. The [Logic component](#logic-component) shows the detailed `delete` sequence.

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T16-2/tp/tree/master/src/main/java/seedu/tuitionbook/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

`CommandBox` uses `CommandTextField`, a `TextField` subclass that converts pasted tabs and line separators to spaces before JavaFX filters them. This preserves word boundaries while keeping each command on one line. Pasted text is inserted into the field but is not executed until the user presses <kbd>Enter</kbd>.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T16-2/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-T16-2/tp/tree/master/src/main/java/seedu/tuitionbook/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

`PersonListPanel` receives both the displayed working set and the complete contact list from `Logic`. It displays the working set while using the complete list to resolve a student's guardian name and contact number and a guardian's linked student names, including related contacts outside the working set. The panel refreshes its cards when the complete list changes so that relationship details remain current.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-T16-2/tp/tree/master/src/main/java/seedu/tuitionbook/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The following sequence diagrams show the same `execute("delete 1")` call in two focused parts so that their labels remain readable at normal browser zoom. The first diagram expands command parsing and uses a reference frame for the remaining execution.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="LogicManager parsing delete 1 and receiving a newly created DeleteCommand" />

*Parsing `delete 1` and creating the `DeleteCommand`.*

The second diagram expands command execution and persistence. Its opening reference frame represents the parsing interactions shown above.

<puml src="diagrams/DeleteExecutionSequenceDiagram.puml" alt="LogicManager executing DeleteCommand, updating the model and saving TuitionBook" />

*Executing the `DeleteCommand` and saving the updated TuitionBook.*

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to a `TuitionBookParser` object, which creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the arguments. It creates `HelpCommand`, `ExitCommand`, and `ReloadCommand` directly instead.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. Before executing a command that can mutate contacts, `LogicManager` copies the contact data and snapshots working-set UUIDs, order, predicate and query for rollback. It calls `execute(model, storage)`; for `DeleteCommand`, the inherited method delegates to `execute(model)`. The inherited delegation is omitted from the second diagram.
1. `DeleteCommand` resolves the displayed index against the working set, identifies affected students from the complete contact list, and passes the resolved `Person` to `Model.deletePerson`. Deleting a guardian clears the affected students' guardian links. The second diagram omits the complete-list lookup and model-internal link updates, which do not change the main command flow.
1. Each `Command` declares whether it requires contact data to be saved. After a command executes successfully, `LogicManager` saves the `Model`'s contact data only when the command requires it. The second diagram shows the successful save path. If saving throws an `IOException` or `AccessDeniedException`, `LogicManager` restores the contacts and exact working set that existed before the command, then throws a `CommandException`; those rollback calls are deliberately omitted from the diagram.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" alt="Representative parser classes and their dependencies"/>

How the parsing works:
* `TuitionBookParser` creates a command-specific parser for `add`, `edit`, `tag`, `untag`, `delete`, `clear`, `find`, `list`, `link`, `unlink`, and `view`. Each implements `Parser<T extends Command>` for its command type. The diagram shows `AddCommandParser` and `TagCommandParser` as representative examples; the remaining command-specific parsers are omitted.
* `AddCommandParser` uses `ArgumentTokenizer`, `ArgumentMultimap`, the `Prefix` constants in `CliSyntax`, and `ParserUtil` to parse arguments and construct an `AddCommand`. `EditCommandParser`, `ListCommandParser`, and `ViewCommandParser` also use these utilities. Other parsers use only the helpers they need; for example, `DeleteCommandParser`, `LinkCommandParser`, and `UnlinkCommandParser` use `ParserUtil` to parse indexes.
* `TagCommandParser` and `UntagCommandParser` delegate argument parsing to the package-private `TagModificationCommandParser` helper, passing their respective command constructor references. The helper uses the same tokenization and validation utilities and invokes the supplied constructor after parsing. It does not implement `Parser`.
* `ArgumentTokenizer` creates an `ArgumentMultimap`, which stores argument values keyed by `Prefix`. `CliSyntax` defines the recognized prefix constants; `ParserUtil` parses values and indexes without depending on `Prefix`.
* `TuitionBookParser` creates `HelpCommand`, `ExitCommand`, and `ReloadCommand` directly, without a command-specific parser, after rejecting non-blank arguments. In all cases it returns the resulting object as a `Command`.

#### Command-input boundary

`CommandTextField` converts pasted tabs and line separators to normal spaces before the text reaches the parser, including inside quoted values. A CRLF pair becomes one space. This GUI conversion preserves word boundaries and does not execute the pasted text.

As a fallback for raw input that reaches it without command-box conversion, `TuitionBookParser` rejects actual line breaks before trimming or dispatching, using Java's `\R` definition (CR/LF, vertical tab, form feed, NEL, line separator and paragraph separator). The error identifies the first line-break code point. This also applies inside quotes and at the input edges; the parser does not split or join multiple commands. Normal GUI paste therefore does not produce this line-break diagnostic.

Only the extracted command word is lowercased with `Locale.ROOT`. Prefixes, arguments, quoted values, tag identity and the exact `confirm` keyword are not case-normalised. This keeps command lookup independent of the computer's locale without changing contact data.

`ParserUtil.checkForUnsupportedSpaces` diagnoses NBSP (`U+00A0`) and ideographic/full-width space (`U+3000`) only in command words, no-argument command tails and positional-index text. It does not scan field values. Indexed command parsers perform this check before wrapping ordinary invalid-index errors in command-specific usage, and `link` checks before splitting its two indexes. `parseIndex` also checks direct callers. Other invisible-character policies and field validation remain outside this change.

These failures occur during parsing, before command execution, model mutation or contact saving. `help`, `exit` and `reload` with extra arguments never produce an executable command. See [Command input and diagnostics](#command-input-and-diagnostics) for manual scenarios.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-T16-2/tp/tree/master/src/main/java/seedu/tuitionbook/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="550" />


The `Model` component,

* stores TuitionBook data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* keeps ordered contact UUIDs, an originating predicate and a copyable refresh command. It resolves displayed UUIDs to the latest `Person` values and exposes an unmodifiable `ObservableList<Person>` for the UI. Ordinary mutations do not reevaluate existing membership; query commands explicitly replace it.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `TuitionBook`, and each `Person` references tags from that list. This lets `TuitionBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T16-2/tp/tree/master/src/main/java/seedu/tuitionbook/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="1000" />

The `Storage` component,
* can save both TuitionBook data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonTuitionBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

`JsonUtil` removes an optional UTF-8 BOM and decodes file bytes with a strict UTF-8 decoder before passing the text to Jackson, while retaining the same string-based parsing for in-memory callers. Invalid UTF-8 and other encoded files are therefore rejected instead of being accepted with replacement characters or silently converted on the next save. The shared mapper also requires the input to end after one JSON value and rejects duplicate property names at any object depth. These checks prevent malformed, trailing, or conflicting content from being accepted and then discarded by a later save. For the configured contact file, parsing failure enters the existing malformed-file recovery path, which moves the original bytes unchanged to an unused sibling backup or blocks saving when that backup cannot be created.

`DataLoadingException` walks its cause chain to produce bounded user guidance for permission denial, invalid contact values, invalid UTF-8 or JSON, and other file-system failures. It does not expose exception types, paths from the cause, or stack traces. `MainApp` carries this guidance through `StartupNotice`; `UiManager` combines it with the actual backup outcome. `ReloadCommand` uses the same guidance while retaining the current contacts and filter after a failed read.

`ApplicationPaths` resolves application-managed files before file logging starts. Packaged execution uses the directory containing the executable JAR as the application home, independent of the process working directory. Development runs from compiled class directories use the process working directory as an explicit fallback. Contact data, legacy data migration, preferences, logs, log rotation files, and the instance lock file therefore share the same application home.

At startup, `MainApp` uses `tuitionbook.json` when it exists. Otherwise, it attempts to move the legacy `addressbook.json` to that current path. A failed move is logged without aborting initialization: the unchanged legacy path is passed to storage for that run, and migration is retried on a later launch. This preserves readable contacts when directory permissions prevent renaming. If the same permissions also prevent saving, the normal command save-failure rollback restores contacts and the exact working set (membership, order and originating query).

### Common classes

Classes used by multiple components are in the `seedu.tuitionbook.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Incremental tag changes

`tag INDEX t/TAG [t/TAG]...` adds tags by set union; `untag INDEX t/TAG [t/TAG]...` removes them by set difference. `add` continues to accept an initial tag set and `edit` continues to replace the whole set. Stored tag identity is case-sensitive, unlike the case-insensitive `list` filter. Duplicate arguments are deduplicated, existing tags are ignored on addition, and absent tags are ignored on removal.

`TagCommandParser` and `UntagCommandParser` delegate index and tag validation to `TagModificationCommandParser`. It uses `ArgumentTokenizer.tokenizeWithQuotedValues` with only `PREFIX_TAG`, protecting embedded prefix-like text inside quoted tags and decoding the same quote and backslash escapes as `add`, `edit` and `list`. Unsupported field-like prefixes outside quotes are rejected rather than absorbed into tags. Every decoded tag is validated before constructing a command, so malformed quotes and invalid or blank tags reject the entire operation. See [Quoted field parsing](#quoted-field-parsing) for the shared quoting rules.

`TagCommand` and `UntagCommand` share execution through `TagModificationCommand`. It resolves the displayed index and modifies a disposable copy of the selected contact's tags. A changed set produces one replacement `Person` retaining its ID, role, relationship, and all other fields, then updates the contact in place without changing working-set membership or order. A no-change result preserves the working set and requests no save. `canMutate()` remains true before execution so `LogicManager` captures the rollback snapshot, while `requiresSave()` is reset each execution and becomes true only after an actual tag change. Existing storage and save-failure rollback apply without a schema change.

`TagCommand` compares requested tags with the original contact's tag set to report only newly added tags in its success message. `UntagCommand` uses the same comparison to report every absent tag, both when other tags are removed and when nothing changes. Shared feedback formatting sorts names and encloses them in double quotes, escaping literal quotes and backslashes for unambiguous display. This formatting does not change tag matching or command parsing. The difference is recomputed on each execution rather than retained as command state.

For each absent tag, `UntagCommand` also compares its spelling case-insensitively with tags remaining on the selected contact. It suggests every matching stored spelling in sorted order, excluding exact tags removed by this execution. Suggestions use the same quoting and escaping as missing-tag feedback and are recomputed on reuse. Tag identity, removal and persistence remain case-sensitive; suggestions do not request a save when nothing changed.

### Viewing contact relationships

`ViewCommandParser` parses `view INDEX [v/]` with the same positive-index rule as other index-based commands. The optional empty `v/` prefix selects full details for linked contacts; the parser rejects duplicate prefixes and values after `v/`.

`ViewCommand` selects the contact from `Model#getFilteredPersonList()`, then reads the full TuitionBook to resolve relationships by persistent contact ID. Thus, the working set determines the selected index but cannot hide a linked guardian or student from the result. The command does not change contact data or the working set.

Without `v/`, the command displays each linked contact as `NAME (CONTACT_NUMBER)`: the linked guardian for a student or linked students in contact-list order for a guardian. With `v/`, it displays each linked contact's full details. It displays `No guardian linked.` or `No students linked.` when the selected contact has no relationship. Missing optional fields use `-`, and tags use one locale-independent, case-insensitive alphabetical order; original spelling breaks ties. Each displayed tag is enclosed in brackets, with literal backslashes and square brackets escaped for unambiguous boundaries.

`ViewCommand` returns a `CommandResult` containing a short feedback message and the formatted detail text. `MainWindow` shows that text in `DetailDisplay`, which replaces `PersonListPanel` in the main content area. A successful command that has no detail text restores `PersonListPanel`; parse, command, index, and save failures leave the currently visible screen unchanged. The list and its active filter remain in the model while the detail view is visible.

### Stable contact working sets

The working set preserves indexes across non-deleting mutations and compacts them after deletion. `ModelManager` stores ordered UUIDs separately from contact values. An explicit `ListCommand` or `FindCommand` establishes its predicate and canonical refresh command together through one model call. Refresh text is generated from immutable parsed criteria, independently of the original input text. List commands normalize role/prefix order and quote and escape tags where needed, preserving internal tag whitespace; find commands join their parsed keywords with single spaces. Matching rules and stored contact values are unchanged. Existing hidden contacts are not automatically inserted when their values change.

`setPerson` updates displayed values in place, including contacts that no longer match. Matching additions append; nonmatching additions remain hidden. The add command checks its own contact UUID and prepares location guidance, independently of contact-list ordering; LogicManager returns that feedback only after any required save succeeds. Deletion removes the UUID and compacts survivors without rerunning the query; deleting a guardian also updates linked students. `setTuitionBook`, used by successful reload and clear, resets to the full unfiltered book. Restart constructs a new full working set. Rejected and unchanged mutations preserve the view.

`PersonListViewState` copies UUID membership/order and retains the predicate and query. On an actual save failure, `LogicManager` restores contacts, then this snapshot, preserving appended rows and retained exceptions exactly. Every query supplies explicit refresh text; there is no implicit show-all fallback for arbitrary predicates. The query accessor is named `getPersonListQueryPredicate()` because retained displayed contacts may no longer match it. Actual membership comes from the displayed list or snapshot UUIDs. The JSON schema is unchanged and contains no working-set state. Refreshing displayed values requires an O(n) UUID lookup; no constant-time or benchmark claim is made.

`PersonListPanel` supplies each card with a notice when its current values fail the originating predicate. Match status is derived from current data, so it clears when a contact matches again and is correct after rollback. Mutating-command feedback gives the query to refresh; hidden additions explain how to locate the saved contact. Deletion warns that subsequent indexes may have changed.

`MainWindow` also keeps a query/count indicator above the shared list/detail area. It reads the canonical refresh command from `Logic#getPersonListViewState()`, the working set from `Logic#getFilteredPersonList()` and the total from `Logic#getPersonList()`. The shared full-list refresh command identifies the unfiltered state; equal displayed and total counts do not imply that a query is inactive. Matching and retained counts partition only the displayed working set, using its originating predicate against current contact values. The indicator is initialized with the window contents and updated in `executeCommand`'s `finally` block, after success, rejection or save-failure rollback has finished. It does not change query membership, storage or focus. Query text stays on one line with a wrapped full-command tooltip, while the count line wraps at narrow widths.

### Contact field validation

`Name`, `Phone`, `Email`, and `Address` are model value objects that enforce their constraints at construction. Command parsers trim surrounding input whitespace before constructing these objects, but do not otherwise normalize or truncate accepted values. Names support Unicode letters, numbers and combining marks plus ordinary ASCII spaces (U+0020) and the documented punctuation, including straight and right curly apostrophes, and contain at most 100 Unicode code points. Other Unicode whitespace is not valid inside a name. Phone validation uses a linear parser shared with identity normalization. It accepts an optional leading `+`; plain or parenthesized digit groups separated by one or more ASCII spaces, one hyphen, or one slash; at most one trailing parenthesized annotation; and an optional numeric `x`, `ext`, or `ext.` extension. An annotation is separated from the main number by at least one ASCII space, contains at least one ASCII letter, and otherwise contains only ASCII letters, digits, and single internal spaces. The parser rejects malformed or nested parentheses, empty groups, doubled or trailing punctuation separators, malformed annotations, unsupported letters, and malformed or repeated extensions. Email validation implements the documented ASCII local-part and domain-label subset, including apostrophes and internal consecutive hyphens used by punycode labels, while rejecting consecutive periods.

Addresses contain at most 200 Unicode code points and must contain at least one meaningful visible code point. `Address` excludes code points for which `Character.isWhitespace` or `Character.isSpaceChar` is true, and code points in the Unicode control (`Cc`) or format (`Cf`) categories, when deciding whether content is nonblank. These excluded code points may coexist with visible text, so legitimate combining marks or formatting characters do not make an otherwise visible address invalid. LF, vertical tab, form feed, CR, NEL, line separator and paragraph separator characters are always rejected so every stored address is single-line.

`NameComparisonUtil#toUnicodeComparisonForm` centralizes the locale-independent transformation used for Unicode name comparison: NFC normalization, lowercase with `Locale.ROOT`, uppercase with `Locale.ROOT`, then NFC normalization again. Lowercasing before uppercasing makes both `Groß` and `GROẞ` become `GROSS`; it also makes `Straße` and `STRASSE` equal and preserves canonical equivalence between composed and decomposed accents. This standard-library sequence is not complete Unicode case folding and does not promise that every linguistically equivalent spelling compares equally.

Duplicate identity and search apply that shared low-level form differently. `Person#normalizeNameForIdentity` first trims surrounding ASCII spaces and collapses consecutive internal ASCII spaces, then applies the Unicode comparison form; the normalized phone must also match. `NameContainsKeywordsPredicate` applies only the Unicode comparison form to the stored name and every original keyword before literal substring matching. Search neither trims nor collapses the stored name and does not call the complete identity normalizer. Both paths preserve the original name for display and storage.

Phone identity removes ASCII spaces, hyphens, slashes, and numeric-group parentheses, ignores the annotation, retains a leading `+`, and converts each supported extension marker to `x` while retaining its digits. Contacts are duplicates only when both their normalized names and normalized phone numbers match.

`JsonAdaptedPerson` uses the same model validators when loading JSON, keeping add, edit, and storage validation consistent. An address remains optional, and `edit INDEX a/` stores no address rather than an empty `Address`. A data file containing an over-limit, whitespace-, control-, or format-only, or multi-line address is rejected through the invalid-data recovery path. If the configured file can be moved to an unused sibling backup, the backup preserves the original bytes and saving new data is allowed. If that backup move fails, the original file remains in place and saving stays blocked until a successful read.

After tokenization and duplicate-prefix checks, `AddCommandParser` collects absent mandatory prefixes in the fixed order `r/ROLE`, `n/NAME`, `p/PHONE`. A structurally valid command with any absent prefixes reports all of them followed by `AddCommand.MESSAGE_USAGE`; malformed preambles and tokenizer failures retain their existing format or parsing errors. Empty supplied values remain field-validation errors, so the parser does not duplicate model validation.

`PersonListCard.fxml` gives the role badge its preferred minimum width while allowing the name label to shrink. Each student card has one relationship row for its guardian's name and contact number, and each guardian card uses the same row for linked student names in contact-list order. Long names, phone numbers, email addresses, relationship summaries, addresses, and tags retain their beginning and use an ellipsis within the card instead of widening it or pushing the badge out. `PersonCard` attaches a wrapped tooltip containing each full value; `view INDEX v/` also provides the full relationship details through the keyboard-driven workflow. Multiple tags continue to flow in locale-independent, case-insensitive alphabetical order, with original spelling as the tie-breaker.

<a id="quoted-name-and-tag-parsing"></a>

### Quoted field parsing

`AddCommandParser` and `EditCommandParser` opt into `ArgumentTokenizer.tokenizeWithQuotedValues` for `PREFIX_NAME`, `PREFIX_ADDRESS` and `PREFIX_TAG`; `ListCommandParser` and the shared `TagModificationCommandParser` for `tag` and `untag` opt in for `PREFIX_TAG`. The tokenizer automatically recognises those quoted prefixes, visits candidate positions in order and ignores positions inside a quoted value. It then removes the enclosing quotes before normal field validation. It rejects unclosed quotes and trailing text outside the closing quote. Other fields retain their existing quote behaviour. The model and JSON store decoded values, so no storage migration is needed.

For these five field-based commands, a prefix-like token is one or more ASCII letters followed immediately by `/`, at the start of the argument string or after an ordinary ASCII space (`U+0020`). Such a token outside a supported quoted value must match a prefix recognised by that command, including case. Unknown candidates join the same position-ordered scan as known prefixes, so quoted content is skipped before an unknown-prefix error can be raised. The error identifies the token and offers either correcting the command syntax or quoting the complete literal name, address or tag. It does not guess which field was intended.

This deliberately changes some unquoted commands: `n/Muhammad s/o Ali` and `t/Advanced Math/Science` now need complete-value quotes. Non-boundary slashes such as `t/Math/Science` and URL punctuation such as `https://` are not new field boundaries. Recognised unquoted prefixes still start separate fields; the parser cannot detect every accidental use of an otherwise supported prefix. The older `tokenize` method retains its existing contract for other callers.

Inside quoted values, `\"` decodes to a literal double quote and `\\` to a literal backslash. Other backslash sequences and all unquoted values are preserved. The closing-quote scan and decoding use the same escape predicate, so an escaped quote cannot end a value or expose embedded prefixes. An escaped final quote without a subsequent closing quote is rejected. Surrounding whitespace is trimmed as before, and decoded values pass through the existing domain validators; escaping does not widen name validation.

Literal quotes already present in JSON are retained: CLI escaping is decoded before storage and does not change the JSON format or reinterpret stored strings. For example, a stored tag `"Sec 4"` can be filtered using `list t/"\"Sec 4\""`, and `a/"\"Block A\""` enters an address with literal quotes around it. Existing addresses are not stripped or normalised on loading. `Tag` removes surrounding ASCII spaces from every tag, including JSON-loaded values, while preserving internal spacing and case-sensitive identity; this makes a stored `" Math "` tag addressable as `Math` without changing quoted-value parsing.

### \[Historical proposal\] Undo/redo feature

Undo/redo is descoped from the final release under [scope decision 4.1](#decision-4-1-undo-and-confirmation). The following inherited design is retained only as a hypothetical implementation discussion: its classes, commands and model methods are not implemented, and it is not a release commitment.

<panel header="Historical design details (not implemented)">

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedTuitionBook`. It extends `TuitionBook` with an undo/redo history, stored internally as an `tuitionBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedTuitionBook#commit()` -- Saves the current TuitionBook state in its history.
* `VersionedTuitionBook#undo()` -- Restores the previous TuitionBook state from its history.
* `VersionedTuitionBook#redo()` -- Restores a previously undone TuitionBook state from its history.

These operations are exposed in the `Model` interface as `Model#commitTuitionBook()`, `Model#undoTuitionBook()` and `Model#redoTuitionBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedTuitionBook` will be initialized with the initial TuitionBook state, and the `currentStatePointer` pointing to that single TuitionBook state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in TuitionBook. The `delete` command calls `Model#commitTuitionBook()`, causing the modified state of TuitionBook after the `delete 5` command executes to be saved in the `tuitionBookStateList`, and the `currentStatePointer` is shifted to the newly inserted TuitionBook state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitTuitionBook()`, causing another modified TuitionBook state to be saved into the `tuitionBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitTuitionBook()`, so TuitionBook state will not be saved into the `tuitionBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoTuitionBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous TuitionBook state, and restores TuitionBook to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial TuitionBook state, then there are no previous TuitionBook states to restore. The `undo` command uses `Model#canUndoTuitionBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoTuitionBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores TuitionBook to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `tuitionBookStateList.size() - 1`, pointing to the latest TuitionBook state, then there are no undone TuitionBook states to restore. The `redo` command uses `Model#canRedoTuitionBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify TuitionBook, such as `list`, will usually not call `Model#commitTuitionBook()`, `Model#undoTuitionBook()` or `Model#redoTuitionBook()`. Thus, the `tuitionBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (choice in the historical proposal):** Saves the entire TuitionBook.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.


</panel>

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, configuration, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [Configuration guide](Configuration.md)
* [DevOps guide](DevOps.md)

### Diagnostic logging and contact clearing

Contact storage and diagnostic logs are separate. `ClearCommand` clears all contacts, including contacts hidden by the current filter, but does not remove logs. See the [Logging guide](Logging.md) for log location, rotation and handling details.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is a private one-to-one tutor who travels to students' homes
* manages a significant number of students and guardians
* needs to maintain student and guardian contact information and their relationships in one place
* needs to retrieve the relevant guardian's contact details and home-address information quickly
* benefits from grouping contacts by academic level, subject, tags, or area
* prefers a single-user desktop application
* can type quickly, prefers typing over other input methods, and is comfortable with a command-based interface
* prefers contact information to be stored locally and restored automatically between application sessions
* uses English and an `en-*` operating-system locale

**Value proposition**: TuitionBook is designed to help private one-to-one home tutors manage student and guardian contacts, organise contacts with roles and tags, and retrieve the correct contact details quickly, instead of using a generic contact list or manually cross-referencing separate records. Guardian links and home addresses are optional. Tutors can label academic levels and subjects using generic tags, for example `t/Sec4 t/AMath` when adding or editing a contact. Dedicated level/subject fields and filtering by those fields are descoped from the final release; they remain possible future enhancements, not promised features. Level and subject grouping is supported through `list r/student t/Sec4 t/AMath`, which requires both tags. See the [tag workflow](UserGuide.md#organising-levels-subjects-and-status-with-tags).

TuitionBook is intended for English-language users and supports `en-*` operating-system locales. Its interface, documentation, and messages are in English; this does not require contact information to contain only English text. Contact fields accept UTF-8 text subject to their documented validation rules. In particular, names accept Unicode letters, numbers, and combining marks plus ordinary ASCII spaces (U+0020), with the accepted capitalization, punctuation, and Unicode representation preserved after surrounding command-input whitespace is trimmed. Duplicate-contact checks apply the documented locale-independent Unicode comparison form after identity-specific ASCII-space normalization; this covers canonically equivalent accents and the demonstrated sharp-S cases without claiming complete Unicode case folding. See the [User Guide](UserGuide.md#adding-a-contact-add).

TuitionBook focuses on contact and relationship management; lesson scheduling, attendance, fees, academic-progress tracking, and messaging are outside its scope.

### User stories

Priorities: High (must have) - `***`, Medium (nice to have) - `**`, Low (unlikely to have) - `*`

Priority represents importance to the target user and does not indicate whether a requirement is currently implemented.

`Scope` is a planning classification, not a claim about the current implementation state. `MVP` identifies requirements in the agreed minimum TuitionBook product, while `Beyond MVP` identifies requirements outside that minimum, including implemented, scheduled, deferred, and descoped stories. The table retains these stories as a record of user needs; inclusion does not promise delivery.

The MVP was delivered in v1.3. The v1.4 implementation also supports unlinking, tag filtering with an optional role, name-substring search, an offline command summary, `clear confirm`, quoted names and tags with supported escapes, incremental `tag`/`untag` commands, and reloading the contact file. These features are implemented in this source revision; the versioned release and its changelog identify what has been published. `find` searches names only, while `list [r/ROLE] [t/TAG]...` requires all supplied tags and the optional role to match; neither combines role and name filtering.

* **Tag-based organisation**: academic level, subject and active/inactive status are supported through generic tags and tag filtering. There are no dedicated academic fields or inactive/archive states. See the scope decisions below for the workflow and its limitations.
* **Descoped**: dedicated level/subject fields, dedicated inactive/archive functionality, sorting, area search, notes, contact-to-clipboard commands, import/export commands and undo are excluded from the final release. Copying the User Guide URL remains supported. These decisions do not exclude generic tags or the implemented in-app command summary.
* **Deferred**: other unimplemented Beyond-MVP stories remain backlog ideas without a promised release or delivery date. Their priority and presence in the table do not imply implementation.

| Priority | Scope | As a … | I want to … | So that I can… |
|----------|-------|--------|-------------|----------------|
| `***` | MVP | private tutor | add a contact as either a student or guardian | record the person's details and distinguish their role clearly |
| `***` | MVP | private tutor | list all the contacts I manage | obtain an overview of my students and their guardians |
| `***` | MVP | private tutor | have my contacts, roles, and guardian links stored locally and saved automatically | continue using the same information after restarting TuitionBook |
| `***` | MVP | private tutor | link a student to at most one guardian | record who is responsible for that student |
| `***` | MVP | private tutor | view a student's details together with the linked guardian's details | contact the appropriate guardian without manually searching through separate records |
| `**` | MVP | private tutor | view a guardian together with the students linked to that guardian | identify all students under that guardian |
| `***` | MVP | private tutor | delete a contact and remove any affected guardian links without deleting other contacts | remove obsolete contact information while keeping the remaining contact records and relationships valid |
| `**` | MVP | private tutor | edit a contact's information | keep the contact's details accurate and up to date |
| `**` | MVP | private tutor | find contacts by name | retrieve a contact without scanning the entire contact list |
| `**` | MVP | private tutor | clear all stored contact data | start over when the existing dataset is no longer needed |
| `**` | MVP | private tutor | access help for TuitionBook | recall how to use the application and its commands |
| `**` | MVP | private tutor | exit TuitionBook using a command | close the application without switching to a mouse-driven action |
| `**` | Beyond MVP | private tutor | filter contacts by student or guardian role | focus on the relevant type of contact |
| `**` | Beyond MVP | private tutor | record a student's academic level and subject or subjects | keep track of what I teach each student |
| `**` | Beyond MVP | private tutor | filter students by academic level and/or subject | find students studying a particular subject at a given level |
| `**` | MVP | private tutor | assign tags to contacts | organise contacts using flexible labels |
| `**` | Beyond MVP | private tutor | unlink a guardian from a student without deleting either contact | correct or remove an outdated relationship while retaining both contacts |
| `*` | Beyond MVP | private tutor | link multiple guardians to one student | represent students who have more than one responsible guardian |
| `*` | Beyond MVP | private tutor | delete multiple contacts in one operation | remove several obsolete contacts efficiently |
| `*` | Beyond MVP | private tutor | delete a contact by name | remove a contact without first locating its displayed index |
| `*` | Beyond MVP | private tutor | search contacts using partial names or other contact information | find contacts when I remember only part of their information |
| `*` | Beyond MVP | private tutor | filter contacts by tag | retrieve a labelled group of contacts quickly |
| `*` | Beyond MVP | private tutor | combine role filtering with name or tag filtering | narrow a large contact list using multiple criteria |
| `*` | Beyond MVP | private tutor | filter students by address or area | find students near a location and plan home visits |
| `*` | Beyond MVP | private tutor | sort contacts using a chosen criterion | locate relevant contacts more efficiently |
| `*` | Beyond MVP | private tutor | receive confirmation before deleting contacts or clearing data | avoid losing contact data by mistake |
| `*` | Beyond MVP | private tutor | undo a deletion or clear operation | recover contact data after an accidental action |
| `*` | Beyond MVP | private tutor | edit a contact's role safely | correct an incorrectly assigned role without creating invalid guardian links |
| `*` | Beyond MVP | private tutor | link a guardian to a student using the guardian's name | create the relationship without first finding the guardian's list index |
| `*` | Beyond MVP | private tutor | view a command's syntax and examples offline | use the command when I cannot access the online User Guide |
| `*` | Beyond MVP | private tutor | copy a contact's phone number conveniently | use the number without manually retyping it |
| `*` | Beyond MVP | private tutor | mark a student as inactive without deleting the contact | keep former students out of my active list while retaining their records |
| `*` | Beyond MVP | private tutor | restore an inactive student | resume managing the student if tuition restarts |
| `*` | Beyond MVP | private tutor | record a short note for a contact | remember relevant context that does not fit the standard contact fields |
| `*` | Beyond MVP | private tutor | export contact data | keep a portable copy of my contact records |
| `*` | Beyond MVP | private tutor | import contact data | restore or migrate contacts without re-entering them individually |
| `*` | Beyond MVP | private tutor | have TuitionBook preserve an unreadable data file | avoid losing the original data while resolving a loading problem |
| `*` | Beyond MVP | private tutor | receive a precise explanation when contact data cannot be loaded | understand what must be corrected |

### Scope decisions for v1.4

This record reconciles [#134](https://github.com/AY2627S1-CS2103T-T16-2/tp/issues/134) with the implemented commands. The US identifiers refer to the original backlog, not row numbers in the table above. These are product decisions and documented limitations, not guarantees about how a future bug report will be evaluated.

#### Decision 4.1: Undo and confirmation

US20 undo/redo is excluded from the final release. There is no command history that can restore a successful deletion or clear operation; users should back up the data file before bulk changes. General confirmation prompts are also excluded. The later [#141](https://github.com/AY2627S1-CS2103T-T16-2/tp/issues/141) decision adds a specific safeguard: `clear confirm` is required, and the message explicitly includes contacts hidden by filters. `delete INDEX` remains immediate. Save-failure rollback is implemented and restores contacts and the exact working set (membership, order and originating query) after a failed save; it is distinct from user-invoked undo and does not reverse a successful command.

#### Help and notes

The earlier US12 descope was reversed by [#93](https://github.com/AY2627S1-CS2103T-T16-2/tp/issues/93): Help now includes an offline command summary, with browser access to the full UG and Copy URL as a fallback. It is a static syntax summary, not a context-sensitive help system.

US29 free-text notes remain excluded. Notes would need a deliberate syntax for text containing field-like prefixes; simply introducing another prefixed field could interpret part of a note as another argument. Name quoting under [#97](https://github.com/AY2627S1-CS2103T-T16-2/tp/issues/97) was extended to tags by [#163](https://github.com/AY2627S1-CS2103T-T16-2/tp/pull/163). The v1.5 development source also supports quoted addresses alongside strict unknown-prefix checks for #193/#201. Quoted names, addresses and tags support `\"` for a literal double quote and `\\` for a literal backslash, subject to field validation. This protects embedded prefixes in those quoted fields; it does not add notes or quoting for every field. See [Quoted field parsing](#quoted-field-parsing).

#### Academic grouping and inactive contacts

US18/US19 needs are supported through generic tags: `t/Sec4 t/AMath` records a level and subject, and `list r/student t/Sec4 t/AMath` selects students with both labels. Dedicated academic fields and validation remain excluded. This updates the value proposition clarified in [#131](https://github.com/AY2627S1-CS2103T-T16-2/tp/pull/131).

`Active` and `Inactive` are ordinary organisational tags. They do not hide contacts, archive records, disable commands, or change guardian relationships. Tutors explicitly select `list r/student t/Active` when needed. Editing tags replaces the full set, so an `edit` changing status must retain every other desired tag, for example `edit 1 t/Inactive t/Sec4 t/AMath`. The later [#164](https://github.com/AY2627S1-CS2103T-T16-2/tp/issues/164) decision, implemented in [#166](https://github.com/AY2627S1-CS2103T-T16-2/tp/pull/166), adds `tag` and `untag` for incremental changes without retyping unrelated tags. These commands match stored tags case-sensitively and do not enforce mutually exclusive status labels. Dedicated archive/restore functionality remains excluded from the final release.

#### Tag identity and compatibility

[#155](https://github.com/AY2627S1-CS2103T-T16-2/tp/issues/155), implemented in [#160](https://github.com/AY2627S1-CS2103T-T16-2/tp/pull/160), deliberately broadens #152's single-hyphen rule to allow natural spaced labels and printable ASCII punctuation. `Tag` trims surrounding ASCII spaces for both command and JSON construction, while internal spaces remain significant (`Sec 4` and `Sec  4` differ). Stored tag identity is case-sensitive; filtering compares complete tags case-insensitively. Under [#163](https://github.com/AY2627S1-CS2103T-T16-2/tp/pull/163), enclosing quotes delimit a value and are not stored; inside quoted values, `\"` decodes to a literal double quote and `\\` to a literal backslash. Recognised unquoted prefixes still start fields; the v1.5 development build rejects unsupported space-separated letter prefixes. Quoting the complete tag protects embedded prefixes in `add`, `edit`, `list`, `tag` and `untag`; see the UG for examples.

Tag length is deliberately unbounded. The validator uses a linear scan, and cards truncate long values with an ellipsis while retaining the full text in a tooltip and detail view. This is not a guarantee of unlimited resource capacity. Surrounding ASCII spaces are trimmed when tags are loaded; tags that differ only by that padding are merged into one, and the merged value is written on the next save. Any other narrowing of validation or identity still requires an explicit compatibility plan.

#### Single-instance operation and manual file editing

`Main` acquires an `ApplicationInstanceGuard` before it first accesses `LogsCenter` or launches JavaFX. The guard opens `.tuitionbook.lock` in the application home with `FileChannel` and calls `tryLock()`. It retains both the channel and `FileLock` around the blocking `Application.launch(...)` call, so the lock covers preference and contact loading, legacy migration, recovery and backups, normal file logging, UI operation, and shutdown preference saving. The try-with-resources boundary also releases the guard if JavaFX initialization or application startup fails; `close()` is idempotent and closes the channel even if lock release reports an I/O failure.

Both a `null` result from `tryLock()` and `OverlappingFileLockException` mean that the same application home is already guarded. `Main` writes a message to standard error and shows a minimal JavaFX error dialog without constructing `MainApp`, `Storage`, or the normal logger. Other I/O or permission failures produce separate actionable guidance and also prevent startup; they do not claim that another instance exists.

The lock file is intentionally retained on shutdown. File existence is not evidence of a running instance: a pre-existing unlocked file is opened and locked normally. Not deleting it avoids a replacement race in which different processes could lock different file objects at the same path. Because each application home has its own lock file, packaged copies in separate folders can run independently.

This guard coordinates only versions of TuitionBook that implement it. It does not protect against external editors or older application versions, merge concurrent changes, or extend to arbitrary external files. The supported manual-editing workflow remains: close TuitionBook, back up and edit the data file, then reopen it. The `reload` command remains an explicit recovery option and replaces in-memory contacts rather than merging changes. Atomic saving protects replacement of an individual data file; it is separate from instance coordination.

#### Startup and unnecessary writes

US47's Week-5 sample-data status is superseded by [#109](https://github.com/AY2627S1-CS2103T-T16-2/tp/issues/109): a missing data file starts with an empty contact list, and a valid file containing an empty contact list stays empty. The application does not insert sample contacts on startup.

Saving is skipped for repeated links, unchanged edits and clearing an empty contact list. Together with `unlink` and the incremental tag commands, contact-changing commands report their actual mutation through `requiresSave()`. Commands that leave contact data unchanged do not attempt a save; filter changes alone do not require saving. Changed and unchanged `edit`, `link`, `unlink`, `tag` and `untag` preserve the working set. Rollback snapshots remain in place for commands that can mutate data, and actual changes retain normal save-failure rollback. Snapshot creation includes quadratic pairwise duplicate validation in addition to O(n) reference copying, so it takes O(n²) time for fixed contact-field sizes. This cost is incurred before execution even when the command ultimately makes no change. No constant-time execution or measured speedup is claimed. See [Saving data](#saving-data) for the full cost explanation, contract and manual checks.

### Use cases

(For all use cases below, the **System** is `TuitionBook` and the **Actor** is the `Private tutor`, unless specified otherwise.)

**Use case: UC01 Add a contact**

**MSS**

1.  User requests to add a contact, providing the contact's role, name, and phone number, and optionally its email address, home address, and tags.
2.  TuitionBook adds the contact and displays confirmation, including its role.

    Use case ends.

**Extensions**

* 1a. A provided detail is invalid, or a mandatory detail is missing.

    * 1a1. TuitionBook shows an error message describing the invalid input.

      Use case ends.

* 1b. A contact with the same normalised name and phone number already exists.

    * 1b1. TuitionBook informs the user that the contact already exists.

      Use case ends.

**Use case: UC02 Link a guardian to a student**

**Preconditions**

* A student and a guardian are visible in the current contact list.

**Guarantee**

* On success, the student is linked to the selected guardian and no student has more than one guardian link.

**MSS**

1.  User requests to link a displayed student to a displayed guardian.
2.  TuitionBook records the guardian link and displays confirmation of the relationship.

    Use case ends.

**Extensions**

* 1a. Either specified index is missing, malformed, or does not refer to a contact in the current list.

    * 1a1. TuitionBook shows an error message.

      Use case ends.

* 1b. The contact selected as the student is not a student, or the contact selected as the guardian is not a guardian.

    * 1b1. TuitionBook shows an error message describing the role mismatch.

      Use case ends.

* 1c. The student is already linked to a different guardian.

    * 1c1. TuitionBook replaces the guardian link and displays confirmation of the replacement relationship.

      Use case ends.

* 1d. The student is already linked to the specified guardian.

    * 1d1. TuitionBook reports that the relationship already exists without changing it.

      Use case ends.

**Use case: UC03 Unlink a guardian from a student**

**Preconditions**

* A student is visible in the current contact list.

**Guarantee**

* On success, the student has no guardian link; neither contact is deleted.

**MSS**

1.  User requests to unlink a displayed student.
2.  TuitionBook removes the guardian link and displays confirmation naming the student and former guardian.

    Use case ends.

**Extensions**

* 1a. The specified index is missing, malformed, or does not refer to a contact in the current list.

    * 1a1. TuitionBook shows an error message.

      Use case ends.

* 1b. The selected contact is not a student.

    * 1b1. TuitionBook shows an error message describing the role mismatch.

      Use case ends.

* 1c. The selected student has no guardian link.

    * 1c1. TuitionBook reports that the student has no linked guardian without changing it.

      Use case ends.

**Use case: UC04 Edit a contact**

**Preconditions**

* At least one contact is visible in the current contact list.

**MSS**

1.  User submits an edit request identifying a contact by its index in the current contact list and providing one or more replacement contact details.
2.  TuitionBook updates the supplied details, retains all unspecified details, and displays confirmation.

    Use case ends.

**Extensions**

* 1a. The specified index does not refer to a contact in the current list.

    * 1a1. TuitionBook shows an error message.

      Use case ends.

* 1b. The request is invalid.

    * 1b1. TuitionBook shows an error message describing the invalid request.

      Use case ends.

* 1c. The requested update would duplicate another contact.

    * 1c1. TuitionBook informs the user that the contact already exists.

      Use case ends.

**Use case: UC05 Find contacts by name**

**MSS**

1.  User submits a find request with one or more name substrings.
2.  TuitionBook applies the documented locale-independent Unicode comparison form to each stored name and requested substring, then displays each contact whose transformed name contains at least one transformed substring using literal matching. Search does not apply duplicate identity's ASCII-space trimming or collapsing. Other contact fields and linked contact names are not searched.

    Use case ends.

**Extensions**

* 1a. No name substring is provided.

    * 1a1. TuitionBook shows an error message describing the invalid request.

      Use case ends.

**Use case: UC06 View contact details**

**Preconditions**

* At least one contact is visible in the current contact list.

**MSS**

1.  User submits a view request identifying a contact by its index in the current contact list.
2.  TuitionBook displays the contact's role, contact details, and tags.
3.  TuitionBook displays relationship details: for a student, the linked guardian's contact information or an indication that no guardian is linked; for a guardian, the names of linked students or an indication that none are linked.

    Use case ends.

**Extensions**

* 1a. The specified index does not refer to a contact in the current list.

    * 1a1. TuitionBook shows an error message.

      Use case ends.

**Use case: UC07 Delete a contact**

**Preconditions**

* At least one contact is visible in the current contact list.

**Guarantee**

* On success, no guardian link refers to a deleted contact.

**MSS**

1.  User requests to delete a displayed contact.
2.  TuitionBook deletes the contact and clears any guardian links involving it, retaining all other contacts.
3.  TuitionBook reports the deletion and, where applicable, the number and names of students whose guardian links were cleared.

    Use case ends.

**Extensions**

* 1a. The specified index does not refer to a contact in the current list.

    * 1a1. TuitionBook shows an error message.

      Use case ends.

* 2a. TuitionBook cannot save the deletion.

    * 2a1. TuitionBook shows a storage error, restores the deleted contact and any guardian links cleared by the request, restores the exact previous working set (membership, order and originating query), and does not report a successful deletion.

      Use case ends.

**Use case: UC08 Filter contacts by role**

**MSS**

1.  User requests contacts with a specified role.
2.  TuitionBook replaces the active filter, displays only contacts with that role, and reports the number of matches.

    Use case ends.

**Extensions**

* 1a. The request is malformed, or the supplied role is invalid.

    * 1a1. TuitionBook shows an error message describing the invalid request and retains the active filter.

      Use case ends.

* 2a. No contact has the specified role.

    * 2a1. TuitionBook displays an empty contact list, reports zero matches, and retains the new filter as the active filter.

      Use case ends.

**Use case: UC09 Filter contacts by tags**

**MSS**

1.  User requests contacts tagged with both `Sec4` and `AMath`, for example by entering `list t/Sec4 t/AMath`.
2.  TuitionBook replaces the active filter, displays only contacts that match every supplied tag by complete name case-insensitively and, if specified, the role, and reports the number of matches.

    Use case ends.

**Extensions**

* 1a. User also specifies a role, for example by entering `list r/student t/Sec4 t/AMath`.

    * 1a1. TuitionBook applies the role together with every supplied tag as the new filter.

      Use case resumes from step 2.

* 1b. The request is malformed, or a supplied tag or role is invalid.

    * 1b1. TuitionBook shows an error message describing the invalid request and retains the active filter.

      Use case ends.

* 2a. No contact matches every supplied criterion.

    * 2a1. TuitionBook displays an empty contact list, reports zero matches, and retains the new filter as the active filter.

      Use case ends.

**Use case: UC10 Reload contact data**

**MSS**

1.  Private tutor submits a reload request.
2.  TuitionBook reads the configured data file, replaces the current contacts with the loaded contacts, and resets the working set to all loaded contacts in contact-list order.
3.  TuitionBook enables saving and reports the number of loaded contacts. If guardian links are repaired, it lists each affected contact's name, UUID, guardian ID when present, and repair reason.

    Use case ends.

**Extensions**

* 2a. The configured data file is missing.

    * 2a1. TuitionBook replaces the current contacts and working set with empty lists, enables saving, and reports that zero contacts were loaded.

      Use case ends.

* 2b. The file contains invalid guardian links but all contacts are otherwise valid.

    * 2b1. TuitionBook loads the contacts without those links, resets the working set to all loaded contacts in contact-list order, and reports the number of links repaired in memory for the tutor to review before saving.
    The result lists every affected contact, contact ID, guardian ID when present, and repair reason for the tutor to review before saving.

      Use case resumes from step 3.

* 2c. TuitionBook cannot read the file but preserves it in a backup.

    * 2c1. TuitionBook retains the current contacts and exact working set (membership, order and originating query), enables saving, and reports the backup path with permission, encoding/JSON, contact-value, or file-system guidance for restoring the preserved data before reloading again.

      Use case ends.

* 2d. TuitionBook cannot read the file or preserve it in a backup.

    * 2d1. TuitionBook retains the current contacts and exact working set (membership, order and originating query), leaves the original file in place, blocks saving, and reports the cause-specific action required before the original is moved or reloaded.

      Use case ends.

See the [malformed-file recovery policy](#malformed-file-recovery-policy) and [saving-data manual test scenarios](#saving-data) for the preservation rules and copyable recovery procedures.

**Use case: UC11 Clear all contacts**

**Preconditions**

* At least one contact is stored. The active filter may hide some contacts.

**Guarantee**

* On success, no contacts remain. TuitionBook does not provide an undo operation for a successful clear.

**MSS**

1.  User requests to clear all contacts using the exact confirmation keyword, by entering `clear confirm`.
2.  TuitionBook removes every stored contact, including contacts hidden by the active filter, saves the empty contact list, and reports completion.

    Use case ends.

**Extensions**

* 1a. The confirmation keyword is missing or incorrect, or the request contains other arguments.

    * 1a1. TuitionBook shows the required `clear confirm` format and retains all contacts and the active filter.

      Use case ends.

* 2a. TuitionBook cannot save the cleared contact list.

    * 2a1. TuitionBook shows a storage error, restores all contacts and guardian links, retains the previously active filter, and does not report a successful clear.

      Use case ends.

**List-filter command rule**

Each `list` query replaces the working set: bare `list` displays all contacts, while `list r/ROLE` and `list t/TAG...` display contacts matching the supplied criteria in contact-list order. Role matching and tag matching is exact and case-insensitive, and all supplied tags must be present. Other trailing text is rejected.

**Startup data rule**

If the configured data file is missing, TuitionBook starts with an empty contact list instead of creating sample contacts. An existing valid data file containing no contacts also remains empty after restart. This rule does not change the separate recovery behaviour for a malformed data file.

**Malformed-file recovery policy**

When the configured data file cannot be read, TuitionBook tries to move it unchanged to a sibling backup named `<data-file>.backup`. If that name is occupied, it tries `<data-file>.backup.1`, then increasing numeric suffixes. After a successful move, the load failure carries the exact backup path and bounded cause-specific guidance to the UI. TuitionBook starts with an empty contact list, can save new data at the configured path, and shows appropriate permission, encoding/JSON, contact-value, or file-system recovery steps in both a warning dialog and the result display. If no backup can be created, it leaves the original file in place, blocks saving so that it cannot be overwritten, and presents the same cause-specific guidance before asking the user to reload or restart.

### Non-Functional Requirements

1.  TuitionBook shall run without an application installer on any mainstream desktop operating system with Java `25` or later installed.
2.  TuitionBook shall support at least 1,000 contacts. On a reference workstation with a dual-core 1.6 GHz processor, 8 GB RAM, and Java `25`, each listing, finding, filtering, viewing, adding, editing, deleting, tagging, untagging, linking, or unlinking command shall complete within two seconds for a data set of 1,000 contacts, measured from command submission to the result being displayed.
3.  All documented add, edit, delete, list, find, filter, view, tag, untag, link, and unlink operations shall be executable through keyboard-entered commands, without requiring a mouse.
4.  TuitionBook shall operate as a single-user desktop application and shall not require a network connection for its normal contact-management functions.
5.  TuitionBook shall store contact data only in a local, human-editable text file and shall not transmit contact data to an external service.
6.  Changes made by successful mutating operations shall be available after a normal application restart. If saving a mutating operation reports a storage error, TuitionBook shall restore the contacts and exact working set (membership, order and originating query) to their state before that operation.
7.  TuitionBook shall recover without terminating unexpectedly when its local data file cannot be loaded cleanly: if the file is missing, it shall start with an empty contact list and show an information dialog; if it is malformed and a backup succeeds, it shall retain a backup of the malformed file, start with an empty data set, show the exact backup path and recovery steps in a warning and result display, and allow saving; if that backup fails, it shall retain the original file, start with an empty data set, show that saving is blocked and how to remedy it, and block saving; if it contains an invalid guardian link, it shall load the valid contacts, omit the invalid link, and show a warning listing every affected contact and repair reason.
8.  TuitionBook shall allow at most one guarded application instance per application home directory while allowing application copies in separate home directories to run independently.

### Glossary

* **Contact**: A record in TuitionBook representing either a student or a guardian, with a name, phone number, and optional email address, home address, and tags.
* **Current contact list**: The displayed working set, including retained contacts that no longer match its originating query; index-based commands refer to positions in this list.
* **Contact ID**: A unique, persistent identifier assigned by TuitionBook to a contact.
* **Data file**: The local text file that stores TuitionBook contacts and their recorded guardian links between application sessions.
* **Filter**: The originating query condition used to establish a working set, classify additions and identify retained contacts that no longer match. TuitionBook supports a name filter or a combined role-and-tag filter. The role is optional, and all supplied tags must match exactly, ignoring case. An explicit query replaces the working set; later edits do not automatically recompute membership.
* **Guardian**: A contact with the `guardian` role. A guardian contact may have zero or more recorded student links; these links record information in TuitionBook and do not establish or determine real-world responsibility.
* **Guardian link**: A recorded association from a student to a guardian contact. A student has at most one guardian link, while a guardian can be linked to multiple students.
* **Home address**: A stored lesson or contact address, usually associated with a student; it can include details such as a unit number, gate code, or lift instructions.
* **Index**: A positive one-based position in the current working set, used by index-based commands. Indexes can change after deletion compacts the working set, an explicit query, a successful reload, clear or restart.
* **Mainstream OS**: Windows, macOS, or a commonly used Linux distribution.
* **Normalised name**: A UTF-8 name converted to its duplicate-identity form: leading and trailing ASCII spaces are removed, each internal run of ASCII spaces is collapsed to one, and the shared Unicode comparison form is applied while the original display value remains unchanged. Search uses only the Unicode comparison form, without this space normalization.
* **Normalised phone number**: A phone number converted to its comparison form before duplicate checking: ASCII spaces, hyphens, slashes, and numeric-group parentheses are removed; the optional annotation is ignored; a leading `+` is retained; and `x`, `ext`, and `ext.` become `x` while retaining the extension digits. Country-code equivalence is not applied in the MVP.
* **Private tutor**: A tutor who provides one-to-one tuition and travels to students' homes.
* **Role**: A contact classification. Users may enter `student` or `guardian` in any casing; TuitionBook stores and displays the corresponding `STUDENT` or `GUARDIAN` role.
* **Student**: A contact with the `student` role who may be linked to one guardian.
* **Working set**: Ordered contact UUIDs displayed with their current values, together with the originating query and canonical refresh command. Matching additions append, nonmatching additions stay hidden, and edits preserve membership and indexes. Deletion compacts survivors; an explicit query replaces membership. Successful reload, clear and restart reset the working set.
* **Tag**: An optional user-defined label attached to a contact for categorisation or filtering. Its name is non-blank printable ASCII (U+0020 to U+007E), including spaces and punctuation. `Tag` validates with a linear scan, without a length limit or recursive regular expression, and trims surrounding ASCII spaces at construction. Internal spacing and spelling are preserved. Optional enclosing quotes protect recognised command prefixes and are removed before validation. JSON-loaded tags use the same normalization, so padding-only duplicates collapse and the normalized set is written on the next successful contact save. `Tag.equals` and `hashCode` remain case-sensitive; list filtering uses exact case-insensitive comparison, requiring every requested tag and the optional role to match. `untag` suggests case variants for absent tags without changing its case-sensitive removal rule. `find` remains name-only.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Use these scenarios to check visible behaviour and persistence. Start with [Launch and shutdown](#launch-and-shutdown), then choose the feature you changed. Use a fresh, disposable folder for each independent scenario and synthetic contacts only; do not run destructive tests against real contact data. Unless stated otherwise, enter commands in the main command box and press Enter. Record the JAR version, operating system, steps and actual result when reporting a failure.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on; testers are expected to do more *exploratory* testing.
</box>

### Command input and diagnostics

Use a fresh, disposable folder with an empty contact list. These steps describe the v1.5 development build, not the released v1.4 JAR.

1. Enter `AdD r/student n/Case Sensitive p/91234567 a/12 Main Road t/Math`, then `LiSt` and `ViEw 1`.<br>
   Expected: One contact is stored and displayed; the name remains `Case Sensitive`, the address `12 Main Road`, and the tag `Math`.
1. Enter `help extra`, `EXIT extra`, and `ReLoAd extra` separately.<br>
   Expected: Each reports an invalid-format error with the relevant usage. The app stays open, no Help window opens, and contacts are not reloaded or changed.
1. Enter `CLEAR CONFIRM` and `ADD R/student n/Rejected p/92345678` separately.<br>
   Expected: Both are rejected. Command-word casing does not relax confirmation or field-prefix syntax; the existing contact is unchanged.
1. For the remaining diagnostic tests, replace each bracketed token with the actual character before submitting; do not type the bracketed name literally. A text editor's Unicode insertion feature can supply these characters.
   * `delete[NBSP]1` and `delete 1[NBSP]`, with `[NBSP]` replaced by U+00A0.
   * `link 1[IDEOGRAPHIC SPACE]2`, with the token replaced by U+3000.<br>
   Expected: The result identifies the unexpected character's code point and how to correct it; no contact is changed or deleted.
1. Enter `EdIt 1 a/Blk[NBSP]12[IDEOGRAPHIC SPACE]Road`, substituting actual U+00A0 and U+3000 characters inside the address.<br>
   Expected: The address is accepted unchanged under its field rules; syntax diagnostics do not normalise address data.
1. Enter `ExIt`, restart the app, and enter `view 1`.<br>
   Expected: Normal exit works, there is still exactly one contact, and its name, tag and updated address retain their case and spacing.

Pasted tabs and line separators are converted to spaces by the command box, including inside quoted names. Verify this separately using [Pasting separators into the command box](#pasting-separators-into-the-command-box); do not expect a U+2028/U+2029 rejection from normal GUI paste. Raw line-break rejection is a parser fallback covered by `TuitionBookParserTest.parseCommand_lineBreaks_reportCodePointBeforeTrimmingOrDispatch` and the rejected-syntax tests in `LogicManagerTest`, including checks that contacts and stored bytes stay unchanged.

### Contact validation and missing add fields

Use a fresh, disposable folder with an empty contact list.

1. Enter `add r/student n/John O’Connor p/+65 (9123) 4567 (HP) ext. 89 e/o'connor@example.xn--bcher-kva a/123 Main Street`.<br>
   Expected: The contact is added with the entered punctuation and phone formatting preserved.
1. Enter `add r/student n/JOHN O’CONNOR p/+65/9123/4567x89`.<br>
   Expected: TuitionBook reports that the contact already exists because phone formatting and the annotation are ignored, and the extension marker is normalized, for identity comparison.
1. Enter `add r/student n/John@Doe p/91234567`, then `add r/student n/John Doe p/(65 9123-4567`, then `add r/student n/John Doe p/9123//4567`, then `add r/student n/John Doe p/91234567 (home  phone)`, then `add r/student n/John Doe p/91234567 e/peter..jack@example.com`.<br>
   Expected: Each command reports the relevant name, phone, or email constraint; no contact is added.
1. Enter `add r/student n/John Doe e/john@example.com a/123 Main Street`, then `add e/john@example.com a/123 Main Street`.<br>
   Expected: The first error identifies `p/PHONE`. The second identifies `r/ROLE, n/NAME, p/PHONE` in that order. Each message includes the ordinary add example before the quoted-name and quoted-tag guidance.
1. Enter `add r/student n/Amy Bee p/91234567`, where the gap in the name is U+2003 EM SPACE.<br>
   Expected: TuitionBook reports the name constraint because names accept ordinary ASCII spaces (U+0020), not Unicode space characters.
1. Close TuitionBook and keep separate copies of the valid `data/tuitionbook.json`. In disposable copies, replace the stored address with each of `"\u2003"`, `"\u200B\uFEFF\u2060\u200E"`, `"\u0000\u001F\u007F"`, and `"Line 1\nLine 2"`. In another copy, use the visible address `"Cafe\u0301\u200B Road"`. Launch TuitionBook against each edited file separately.<br>
   Expected: The whitespace-only, format-only, control-only, and line-breaking addresses follow the invalid-data recovery path. The visible `Café Road` address loads with its combining and format code points preserved. A successful invalid-file backup preserves the original bytes and permits new saving; a failed backup leaves the original in place and blocks saving.

### Strict prefixes and quoted addresses

Use a fresh, disposable folder with an empty contact list. These steps target the v1.5 development build, not the published v1.4 JAR.

1. Enter `add r/student n/Amy p/91234567 a/"20 Main Road, c/o Rina a/p Kaur" t/"Advanced Math/Science"`, then `view 1`.<br>
   Expected: One student is stored. The address is `20 Main Road, c/o Rina a/p Kaur`, the phone is `91234567` and the tag is `Advanced Math/Science`; enclosing quotes are absent.
1. Enter each command separately: `edit 1 a/12 Road ee/amy@example.com`, `tag 1 t/Math tt/Science`, `untag 1 t/Math tt/Science` and `list t/Advanced Math/Science`.<br>
   Expected: Each rejects the unknown prefix (`ee/`, `tt/` or `Math/`) without changing contacts or the working set. No suspected typo is guessed or saved as field data.
1. Enter `list t/"Advanced Math/Science"`.<br>
   Expected: Amy is listed; quoting preserves the complete literal tag.
1. Enter `tag 1 t/"call p/next week"`, then `untag 1 t/"call p/next week"`.<br>
   Expected: The complete literal tag is added and removed; `p/` is not interpreted as a phone field.
1. Enter `edit 1 a/"Unit \"A\" C:\\Road\\"`, then `view 1`.<br>
   Expected: The address is `Unit "A" C:\Road\`, with literal quotation marks and single backslashes. Other contact details are unchanged.
1. Enter `edit 1 a/"12 Road` and `edit 1 a/"12 Road" extra` separately.<br>
   Expected: Helpful quote-format errors appear and the last valid address remains.
1. Exit and restart, then enter `view 1`.<br>
   Expected: The escaped address and original tag persist unchanged.
1. Enter `edit 1 a/""`, then `view 1`.<br>
   Expected: The address is absent, matching the existing `edit 1 a/` clearing behaviour.

### Quoted names and tags

Use a fresh, disposable folder with an empty contact list.

1. Enter `add r/student n/"Rina a/p Kaur" p/91234567 a/123 Main Street`.<br>
   Expected: The name is `Rina a/p Kaur` and the address is `123 Main Street`; neither includes quotation marks.
1. Enter `add r/guardian n/Guardian p/98765432`, then `link 1 2`.
1. Enter `edit 1 n/"Rina s/o Kaur"`, then `view 1`.<br>
   Expected: The full updated name appears; the phone, address and guardian link are unchanged.
1. Exit and restart the app, then enter `view 1`.<br>
   Expected: The full updated name and guardian link remain.
1. Enter `edit 1 n/"Rina a/p Kaur`.<br>
   Expected: An error explains the missing closing double quote; the contact is unchanged.
1. Enter `edit 1 n/"Rina" extra`.<br>
   Expected: An error explains that a quoted value must be followed by another field or the end of the command; the contact is unchanged.
1. Enter `add r/student n/Rina a/p Kaur p/90000001`.<br>
   Expected: Existing unquoted parsing applies: name `Rina`, address `p Kaur`. This is the documented limitation; quotation marks are required to enter that full name.
1. Enter `edit 1 t/"meet at a/c room" t/"call p/next week"`.<br>
   Expected: Both complete tags are stored without quotation marks; the contact's address and phone number are unchanged.
1. Enter `list t/"meet at a/c room"`.<br>
   Expected: Rina appears because the quoted filter is parsed as one complete tag.
1. Enter `edit 1 t/"\"Sec 4\"" t/"C:\\Math\\"`, then `list t/"\"Sec 4\""`.<br>
   Expected: Rina has tags `"Sec 4"` (including the literal quotation marks) and `C:\Math\`; the filter matches Rina.
1. Exit and restart, then enter `list t/"\"Sec 4\"" t/"C:\\Math\\"`.<br>
   Expected: Rina still matches both complete tags; escaping has not added backslashes to the stored quotation marks or doubled the stored backslashes.
1. Enter `edit 1 t/"Math\"`.<br>
   Expected: The missing-closing-quote error appears because the final quote is escaped; the contact is unchanged.

### Clearing all contacts

Use a fresh, disposable folder with an empty contact list.

1. Enter `add r/guardian n/Grace Guardian p/90000001`, then enter `add r/student n/Sam Student p/90000002`.
1. Enter `list r/student`.<br>
   Expected: Only Sam Student is visible; Grace Guardian remains stored but hidden by the active filter.
1. Enter `clear`, `clear   `, `clear CONFIRM`, `clear Confirm`, `clear confirm confirm`, `clear confirm extra`, `clear yes`, `clearconfirm`, `clear confirmation`, and `clear\tno` one at a time.<br>
   Expected: Bare or whitespace-only `clear` reports `Confirmation required: enter "clear confirm" to clear all contacts.` Each other nonblank argument reports `Invalid confirmation: enter "clear confirm" to clear all contacts.` Neither contact is deleted, and the student filter remains active.
2. Enter `clear confirm`.<br>
   Expected: All contacts are deleted, including Grace Guardian, who was hidden by the filter. The result states that contacts hidden by the current filter were also cleared.
3. Exit and restart TuitionBook.<br>
   Expected: The contact list remains empty.

### Launch and shutdown

Use the packaged JAR in disposable folders for every scenario in this section. Keep any synthetic contacts in those folders only.

1. Duplicate launch from one application home

   1. Put the JAR in an empty folder, launch it with `java -jar "tuitionbook.jar"`, dismiss the empty-data notice, and add `add r/student n/Alex Lock Test p/91234567`.

   1. Keep the first window open. From a second terminal, run the same launch command against the same JAR.<br>
      Expected: The second launch promptly prints that TuitionBook is already running and shows an error telling you to use or close the existing window. It does not open a normal TuitionBook window. The first window remains usable, and its contact and window preferences are unchanged.

1. Relaunch after normal exit

   1. Close the first window normally or enter `exit`, then run the same launch command again.<br>
      Expected: TuitionBook opens normally with Alex Lock Test present. `.tuitionbook.lock` may remain in the JAR folder; its presence does not block startup and it is not necessary to delete it.

1. Relaunch after forced termination

   1. With TuitionBook running, terminate its Java process using the operating system's task manager or force-quit control. Do not delete `.tuitionbook.lock`.

   1. Run the same launch command again.<br>
      Expected: TuitionBook opens normally because the operating system released the process's file lock. Data from commands that completed successfully before termination remains available; no guarantee is made for an operation interrupted during the forced termination.

1. Independent application homes

   1. Create two empty folders, copy the JAR into each one, and launch both copies while keeping both windows open.<br>
      Expected: Both instances open normally. Adding a synthetic contact in one does not add it to the other; each folder has its own `.tuitionbook.lock`, `preferences.json`, and `data/tuitionbook.json`.

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Open a terminal and run `java -jar` followed by the quoted path to the JAR, as described in the UG's [Quick start](UserGuide.md#quick-start).<br>
      Expected: The GUI opens with an empty contact list and an information dialog explaining that no data file was found. The window size may not be optimal.

   1. Dismiss the dialog and immediately type `list` without clicking the main window.<br>
      Expected: Typing appears in the command box and Enter runs the command.

   1. Relaunch, dismiss the dialog, and press <kbd>Space</kbd>.<br>
      Expected: Help does not open. The command box retains focus.

   1. Press <kbd>F1</kbd>.<br>
      Expected: The Help window opens.

1. Fatal startup error handling

   1. Make a disposable copy of the downloaded JAR and remove `view/MainWindow.fxml` from the copy without changing the original JAR.

   1. Launch the modified copy with `java -jar`.<br>
      Expected: TuitionBook shows a fatal startup error dialog containing the original resource-loading error and then exits cleanly. No secondary `NullPointerException` about an uninitialized main window is shown.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app using the same terminal command.<br>
       Expected: The most recent window size and location are retained.

1. Recovering from null window preferences

   1. Close TuitionBook. In the folder containing the JAR, replace the contents of `preferences.json` with `{"guiSettings": null}`.

   1. Relaunch the app using the same terminal command.<br>
      Expected: TuitionBook opens using the default window settings without a `NullPointerException`. Existing contacts remain unchanged.

   1. Close TuitionBook and inspect `preferences.json`.<br>
      Expected: `guiSettings` contains saved window settings instead of `null`.

### Getting help

1. Click the top-level `Help` control once.<br>
   Expected: The Help window opens immediately without a dropdown. It contains a scrollable summary covering `add`, `list`, `view`, `edit`, `tag`, `untag`, `link`, `unlink`, `find`, `delete`, `clear`, `help`, `reload`, and `exit`, followed by a note on quoted values. The Help window becomes active, so focus leaves the main TuitionBook window.
1. Minimize the Help window, focus the command box, and press <kbd>F1</kbd>.<br>
   Expected: The same Help window is restored and focused; no second Help window appears.
1. Close the Help window. In the main TuitionBook window, focus the command box and enter `help`. The Help window becomes active. Return to the main TuitionBook window, focus its command box, and enter `help` again.<br>
   Expected: The first command opens the Help window and reports `Opened help window.` The second command focuses the same window.
1. Click `Open User Guide`.<br>
   Expected: The User Guide opens in the system default browser. If the desktop environment cannot open a browser, TuitionBook remains open, displays fallback guidance, and leaves `Copy URL` available.
1. Click `Copy URL`, paste into a text editor, and confirm that the copied value is `https://ay2627s1-cs2103t-t16-2.github.io/tp/UserGuide.html`.
1. With the Help window open, activate the main TuitionBook window and select its `File` → `Exit`.<br>
   Expected: Both the main window and Help window close.
1. Relaunch, dismiss any startup notice, then use <kbd>Shift</kbd>+<kbd>Tab</kbd> until the top-level `Help` control is focused and press <kbd>Space</kbd>.<br>
   Expected: Keyboard traversal can still reach and activate Help.

### Displaying wrapped tags

Use a fresh, disposable folder and start the application with an empty contact list. If the environment supports it, repeat this scenario at 1280×720 with 150% display scaling.

1. Enter `add r/student n/Six Tags p/90000001 t/"Active Tuition Student" t/"Secondary Four Level" t/"Additional Mathematics" t/"English Language Study" t/"Physics Practical Prep" t/"Chemistry Practical Prep"`.
1. Enter `add r/student n/One Tag p/90000002 t/Active`, then `add r/student n/No Tags p/90000003`.
1. Resize the application to its minimum width.<br>
   Expected: Six Tags' labels wrap above the phone line without obscuring any contact detail or the next card. Its card grows to fit the extra rows, while One Tag and No Tags remain compact. Tags remain alphabetically sorted.
1. Resize the application wide, then back to its minimum width.<br>
   Expected: The Six Tags card shrinks and grows with the number of tag rows. No tags or contact details overlap at either width.
1. Add one tag whose value is wider than the card, resize to the minimum width, and hover over the shortened tag.<br>
   Expected: The tag ends with an ellipsis and its tooltip shows the complete value.

### Pasting separators into the command box

Use a fresh, disposable folder with an empty contact list. Use synthetic contact data only.

1. Copy `Wei Ming` and `Tan` from two adjacent spreadsheet cells, then enter `add r/student n/`, paste the cells, type ` p/91234567`, and inspect the command box.<br>
   Expected: The command box shows `add r/student n/Wei Ming Tan p/91234567`; the tab is displayed as a space and the command has not run yet.
1. Press <kbd>Enter</kbd>.<br>
   Expected: One student named `Wei Ming Tan` is added. The name is not saved as `Wei MingTan`.
1. Repeat with pasted text containing a tab, line feed, carriage return, CRLF, vertical tab, form feed, NEL, Unicode line separator, and Unicode paragraph separator between two words.<br>
   Expected: Each separator becomes a space, adjacent words remain separate, and no command runs until <kbd>Enter</kbd> is pressed.
1. Select part of an existing command and paste text containing one of the separators.<br>
   Expected: The selected text is replaced, the separator becomes a space, and the caret follows the inserted text.

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. Deleting a guardian with linked students

   1. Prerequisites: In a fresh, disposable folder, enter `add r/guardian n/Grace Guardian p/90000001`, `add r/student n/Sam Student p/90000002`, and `link 2 1`.

   1. Test case: `delete 1`<br>
      Expected: Grace Guardian is deleted. Sam Student remains and his card shows `Guardian: -`; the status message reports the deletion and the affected student.

   1. Restart the application.<br>
      Expected: Sam Student remains unlinked; the deleted guardian does not reappear.

### Displaying contact relationships in the list

Use a fresh, disposable folder and start the application with an empty contact list. Do not use real contact data.

1. Enter `add r/guardian n/Grace Guardian p/90000001`, `add r/student n/Sam Student p/90000002`, `add r/student n/Alex Student p/90000003`, `link 2 1`, and `link 3 1`.<br>
   Expected: Sam Student's and Alex Student's cards display `Guardian: Grace Guardian (90000001)`. Grace Guardian's card displays `Students: Sam Student, Alex Student` in contact-list order.
1. Enter `list r/guardian`.<br>
   Expected: Only Grace Guardian's card remains visible, and its `Students:` line still displays both students.
1. Enter `list`, followed by `unlink 2`.<br>
   Expected: Grace Guardian's `Students:` line updates to `Students: Alex Student`.
1. Enter `unlink 3`.<br>
   Expected: Grace Guardian's `Students:` line updates to `Students: -`.
1. Add and link enough synthetic students to Grace Guardian for the `Students:` line to exceed the card width, then resize the window to the smallest supported size.<br>
   Expected: The relationship stays on one line and ends with an ellipsis instead of widening the card. Hovering over the line shows every linked student name, and `view 1` provides the complete list with contact numbers through the keyboard workflow.

### Viewing a contact

Use a fresh, disposable folder and start the application with an empty contact list. Do not use real contact data. Enter these commands to create the contacts used below:

1. `add r/student n/Alex Yeoh p/87438807 t/zebra t/Alpha t/alpha t/10 t/!tag t/"Math, Science" t/"C:\\Math\\"`
1. `add r/guardian n/Bernice Yu p/99272758`
1. `link 1 2`
1. `add r/student n/Charlotte Oliveiro p/93210283`
1. `add r/guardian n/David Li p/91031282`

1. Enter `view 1`.<br>
   Expected: The contact list is replaced by Alex Yeoh's details and `Bernice Yu (99272758)`. The result area displays `Viewing contact: Alex Yeoh`.
1. Enter `view 1 v/`.<br>
   Expected: Bernice Yu's full guardian details appear after Alex Yeoh's details. Alex's tags appear as `[!tag], [10], [Alpha], [alpha], [C:\\\\Math\\\\], [Math, Science], [zebra]`, regardless of the system locale. The tag containing a comma remains one bracketed tag, and backslashes are escaped.
1. Enter `view 2`.<br>
   Expected: The detail view shows Bernice Yu's details, followed by `Alex Yeoh (87438807)` under linked students.
1. Enter `view 2 v/`.<br>
   Expected: Alex Yeoh's full contact details appear under linked students.
1. Enter `view 3`, then `view 4`.<br>
   Expected: The unlinked student reports `No guardian linked.` and the guardian without students reports `No students linked.`
1. Enter `list r/student`, then `view 1 v/`.<br>
   Expected: Bernice Yu's details still appear. Enter `help`, close the help window, and confirm that the restored list remains filtered to students. Note that `add`, `edit`, `link`, and `unlink` clear the active filter, while `list` and `find` replace it, so they are unsuitable for this check.
1. Enter `view 0`, `view 99`, `view`, `view 1 extra`, and `view 1 v/details` while the detail view is visible.<br>
   Expected: Each command reports an index or format error and leaves the detail view visible; no contact data changes.

### Unicode duplicate identity

Use a fresh, disposable folder and start the application with an empty contact list. Do not use real contact data.

1. Enter `add r/student n/Groß p/91234567`.<br>
   Expected: The contact is added and displayed exactly as `Groß`.
1. Enter `add r/guardian n/GROẞ p/91234567`.<br>
   Expected: The command is rejected as a duplicate because the Unicode name comparison form and normalized phone match. The stored `Groß` contact remains unchanged.
1. Enter `add r/guardian n/GROẞ p/92345678`.<br>
   Expected: The contact is added because its normalized phone differs, and both original name spellings are displayed unchanged.

### Finding contacts by name substring

Use a fresh, disposable folder and start the application with an empty contact list. Do not use real contact data. Enter these commands to create the contacts used below:

1. `add r/guardian n/Grace Tan p/91230001 e/parent@example.com a/Needle Street t/Family`
1. `add r/student n/Bob Lim p/91230002 e/student@example.com a/Example Road t/Math`
1. `link 2 1`
1. `add r/student n/A.B Tan p/91230003`
1. `add r/student n/Işık p/91230004`
1. `add r/student n/Straße Weiss p/91230005`
1. `add r/student n/GROẞ Klein p/91230006`
1. `add r/student n/José Tan p/91230007` (the `é` is entered as `e` followed by a combining acute accent)

1. Enter `find gr`, `find rac`, `find ace`, and `find GRACE` one at a time.<br>
   Expected: Each command shows only Grace Tan and reports `1 person(s) listed!`.
1. Enter `find tan`.<br>
   Expected: Grace Tan and A.B Tan are shown in their original order and the result reports `2 person(s) listed!`.
1. Enter `find rac bob`.<br>
   Expected: Grace Tan and Bob Lim are shown once each in their original order and the result reports `2 person(s) listed!`.
1. Enter `find ışık`, then `find ışı`.<br>
   Expected: Each command shows only Işık and reports `1 person(s) listed!`; matching is Unicode-aware and case-insensitive, including for a partial name.
1. Enter `find STRASSE`, then `find Groß`.<br>
   Expected: The first command shows only Straße Weiss, and the second shows only GROẞ Klein. Their displayed spellings remain unchanged.
1. Enter `find José` using a composed `é`.<br>
   Expected: José Tan is shown even though the stored name uses a decomposed accent, and its stored spelling remains unchanged.
1. Enter `find .`, then `find .*`.<br>
   Expected: The first command shows only A.B Tan. The second command shows no contacts because `.*` is matched literally rather than interpreted as a pattern.
1. Enter `find 91230002`, `find student@example.com`, `find Road`, `find Math`, and `find student` one at a time.<br>
   Expected: Each command shows no contacts and reports `0 person(s) listed!`; phone, email, address, tag, and role fields are not searched.
1. Enter `find grace`.<br>
   Expected: Only Grace Tan is shown; Bob Lim is not matched through the guardian link.
1. Enter `find bob`, then `list`.<br>
   Expected: `find bob` replaces the previous filter and shows only Bob Lim. `list` restores all seven contacts in their original order. None of the contacts or the guardian link has changed.
1. Enter `find`, then enter `find` followed only by spaces.<br>
   Expected: Each command reports an invalid-command format message containing the updated `find` usage, including `Parameters: KEYWORD [MORE_KEYWORDS]...` and `Example: find rac bob`, and does not change the displayed list.

### Feedback for unsupported role and guardian edits

Use an empty disposable book.

1. Enter `add r/student n/Alex Student p/90000001`, `add r/guardian n/Grace Guardian p/90000002`, then `link 1 2`.
1. Enter `list r/student`, then `edit 1 r/guardian`.<br>
   Expected: The result explains that a contact's role cannot be changed after it is added. Alex remains a student linked to Grace; the student-only working set stays unchanged.
1. Enter `edit 1 g/2`.<br>
   Expected: The result directs you to `link STUDENT_INDEX GUARDIAN_INDEX` or `unlink STUDENT_INDEX`, using displayed indexes. No contact or link changes. To use `link`, first show both contacts with `list`; Grace is hidden in the current student-only list.
1. Enter `edit 1 n/Changed r/guardian g/2`.<br>
   Expected: Both explanations appear. Alex's name, role, guardian link and the displayed working set remain unchanged; no partial edit is saved.
1. Enter `edit 1 t/"call r/guardian g/2"`.<br>
   Expected: The quoted text is saved as a literal tag; no unsupported-edit error is shown. Restart and check that Alex still has the original name, student role and guardian link, and the new tag.

### Editing without losing scroll position

Use at least 120 synthetic contacts in disposable storage. Scroll near the bottom so that contact 91 is visible and note the first visible contact and scrollbar position. Enter `edit 91 p/98888888`.<br>
Expected: the phone updates in place and the viewport stays at the same part of the list. Repeat with another visible contact. Explicit queries, deletion compaction and adding a contact are separate view operations; this check concerns edits that keep the same working-set membership and card height.

### Persistent query and contact counts

Use a fresh, disposable folder with an empty contact list.

1. Confirm the indicator above the contact list says `All contacts: 0`.
1. Enter `add r/student n/Alex Student p/90000001 t/Active`, `add r/student n/John Student p/90000002 t/Active`, then `list r/student`.<br>
   Expected: The indicator shows `Query: list r/student` and `Shown: 2 of 2 contacts · Matching: 2 · Retained: 0`. A query remains active even when every contact matches.
1. Enter `add r/guardian n/Grace Guardian p/90000003`, then `list t/Active`.<br>
   Expected: The indicator shows `Query: list t/Active` and `Shown: 2 of 3 contacts · Matching: 2 · Retained: 0`.
1. Enter `untag 1 t/Active`, then `view 1`.<br>
   Expected: The indicator remains visible above the details and shows `Shown: 2 of 3 contacts · Matching: 1 · Retained: 1`. Alex is retained rather than counted as a query match.
1. Enter `tag 1 t/Active`, then `add r/student n/Eve Student p/90000004 t/Active`.<br>
   Expected: The list returns and the indicator shows `Shown: 3 of 4 contacts · Matching: 3 · Retained: 0`.
1. Enter `delete 2`, then `edit 99 n/Missing Student`.<br>
   Expected: After deletion, `Shown: 2 of 3 contacts · Matching: 2 · Retained: 0` is displayed. The rejected edit changes neither the query nor its counts.
1. Enter `find Missing`, then `reload`.<br>
   Expected: The empty query result shows `Query: find Missing` and `Shown: 0 of 3 contacts · Matching: 0 · Retained: 0`. Successful reload resets the indicator to `All contacts: 3`.
1. Enter `clear confirm`, then restart.<br>
   Expected: Both clearing and restarting show `All contacts: 0`. No query state is persisted.

The automated `ContactViewStatusTest` also injects save failures and a failed reload, verifying that the indicator describes the restored contacts and working set rather than an unsuccessful attempted change. It checks a long query at a narrow window width without hiding the counts.

### Stable indexes across contact changes

Use an empty disposable book. These scenarios are adapted from [TAB #183](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/183) and [TAB #315](https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/315).

1. Enter `add r/student n/Alex Student p/90000001 t/Active`, `add r/student n/John Student p/90000002 t/Active`, then `find John`.
1. Enter `edit 1 p/9000002`, then `edit 1 p/90000002`.<br>
   Expected: Both edits target John at index 1. Alex's phone remains unchanged.
1. Enter `list t/Active`, `untag 1 t/Active`, then `tag 1 t/Inactive`.<br>
   Expected: Alex remains index 1 and John remains index 2. Alex's card says it no longer matches; feedback provides `list t/Active` to refresh.
1. Enter `tag 1 t/Active`.<br>
   Expected: Alex keeps index 1 and its notice disappears.
1. Enter `add r/student n/Eve Student p/90000003 t/Active`, then `add r/guardian n/Grace Guardian p/90000004`.<br>
   Expected: Eve appends at index 3. Grace is saved but hidden, with location guidance. Alex and John keep their indexes.
1. Enter `delete 2`.<br>
   Expected: John is deleted, Alex remains index 1, Eve becomes index 2, and feedback warns about changed indexes. Grace stays hidden.
1. Enter `list`, `link 1 3`, `list r/student`, then `unlink 1`.<br>
   Expected: The student-only working set and both indexes remain unchanged; relationship summaries update.
1. Exit and restart.<br>
   Expected: The full list shows Alex, Eve and Grace. John remains deleted and Alex remains unlinked. The working set was not persisted.

### Removing a guardian link

Use a fresh, disposable folder and start the application with an empty contact list. Do not use real contact data.

1. Enter `add r/student n/Alex Yeoh p/87438807`, `add r/guardian n/Bernice Yu p/99272758`, and `link 1 2`.<br>
   Expected: Alex Yeoh is linked to Bernice Yu.
1. Enter `list r/student`, then `unlink 1`.<br>
   Expected: The result area displays `Unlinked student Alex Yeoh from guardian Bernice Yu.` Only Alex Yeoh remains displayed at index 1; both contacts remain stored, and Alex has no guardian link.
1. Enter `list r/student`, then `unlink 1` again.<br>
   Expected: The result area displays `Student Alex Yeoh has no linked guardian.` The list remains filtered to Alex Yeoh; neither contact changes.
1. Close and relaunch the application, then enter `view 1`.<br>
   Expected: Alex Yeoh's details report `No guardian linked.`, confirming that the removed link remains absent after restart.

### Adding and removing individual tags

Use an empty contact list in disposable storage.

1. Enter `add r/guardian n/Grace Guardian p/90000001`, `add r/student n/Sam Student p/90000002 t/Math t/Sec 4`, and `link 2 1`.
1. Enter `find Sam`, then `tag 1 t/Science t/Math`.<br>
   Expected: Feedback is `Added tags to Sam Student: "Science".` Sam has `Math`, `Sec 4`, and `Science`. Only Sam stays displayed at index 1; his ID, phone, and guardian link are unchanged.
1. Enter `list r/student`, then `untag 1 t/Math t/absent`.<br>
   Expected: Sam has only `Sec 4` and `Science`; feedback includes `Tags not present: "absent".` and only Sam stays displayed at index 1.
1. Enter `find Sam`, then `tag 1 t/Science` and `untag 1 t/science`.<br>
   Expected: Both commands report no change, only Sam stays displayed, and no save is attempted. The differently cased `science` does not remove `Science`; `untag` feedback includes `Tags not present: "science". Tag "science" not found, did you mean "Science"?`
1. Enter `tag 1 t/New t/` and `untag 1 t/Science t/`.<br>
   Expected: Both commands report tag constraint errors; no tags are added or removed.
1. Restart, then enter `view 2`.<br>
   Expected: Sam still has `Sec 4` and `Science` and remains linked to Grace.
1. Enter `edit 2 t/Only`, then `untag 2 t/Only`.<br>
   Expected: `edit` replaces the entire tag set with `Only`; `untag` then leaves no tags.
1. Enter `tag 2 t/"study t/weekly" t/"\"Sec 4\"" t/"C:\\Math\\"`, then restart.<br>
   Expected: Sam has exactly three tags: `study t/weekly`, `"Sec 4"` (including literal quotes), and `C:\Math\` (including the final backslash). His other details and guardian link are unchanged.
1. Enter `list t/"study t/weekly"`, then `untag 1 t/"study t/weekly" t/"\"Sec 4\"" t/"C:\\Math\\"`, then restart.<br>
   Expected: The filtered list initially contains only Sam. Removing the tags retains Sam at index 1 with a no-longer-matching notice and a copyable refresh command. After restart, the full list returns and Sam has no tags.
1. Enter `tag 2 t/New t/"unfinished` and `untag 2 t/Math t/"extra" text`.<br>
   Expected: The first command reports a missing closing quote; the second reports extra text after a quoted value. Neither changes any contacts or tags.

### Filtering contacts by tags

Use a fresh, disposable folder with an empty contact list. Enter these commands in order:

```text
add r/student n/Alex Tan p/91234561 t/active t/math
add r/student n/Beth Lim p/91234562 t/active
add r/guardian n/Chris Ong p/91234563 t/active t/math
add r/student n/Dana Lee p/91234564 t/inactive t/math
```

1. Enter `list r/guardian`, then `list r/student t/ACTIVE t/math`.<br>
   Expected: only Alex Tan appears, with `1 person(s) listed!`. The tag-and-role filter replaces the guardian filter. Beth lacks math, Chris has the wrong role, and Dana's inactive tag is not an exact match for active.
1. Enter `list`.<br>
   Expected: Alex Tan, Beth Lim, Chris Ong, and Dana Lee appear in that order, with `Listed all persons.`.
1. Enter `find Beth`, then `list t/math`.<br>
   Expected: Alex Tan, Chris Ong, and Dana Lee appear in that order, with `3 person(s) listed!`. The tag filter replaces the name filter.
1. Enter `list t/absent`.<br>
   Expected: an empty displayed list and the successful result `0 person(s) listed!`.
1. Enter `list` again.<br>
   Expected: all four contacts return in their original order; none were deleted.

### Adding, editing, and filtering tags with spaces and punctuation

Tags support spaces and printable ASCII punctuation so tutors can use natural spaced labels. Existing tags remain valid without changing the JSON format. Once users save these broader tag names, reverting to the previous single-hyphen restriction would reject valid saved contacts; any future narrowing requires an explicit compatibility or migration plan. Internal spaces remain significant. Optional enclosing quotes are command syntax and are not stored.

Internal spacing is deliberately not normalized: `Sec 4` (one space) and `Sec  4` (two spaces) represent distinct tag strings. Case-insensitive list filtering does not collapse spaces, so a filter for one does not match the other. Command parsing trims surrounding whitespace only. Preserve this distinction in validation, persistence, and filtering tests; changing it would alter tag identity and matching behaviour.

Use a fresh, disposable folder and start the application with an empty contact list. Do not use real contact data.

1. Enter `add r/student n/Alex Lim p/91234567 t/Sec 4 t/Math--Science`.<br>
   Expected: Alex Lim is added with both tags, preserving their spelling and capitalization.
1. Enter `edit 1 t/Sec 4 t/Math/Science t/C++`.<br>
   Expected: The existing tags are replaced with `Sec 4`, `Math/Science`, and `C++`.
1. Enter `list r/student t/SEC 4 t/math/science`.<br>
   Expected: Alex Lim appears and the result reports `1 person(s) listed!`.
1. Enter `list t/Sec  4` (two spaces between `Sec` and `4`), then `list t/C+`.<br>
   Expected: Both commands report `0 person(s) listed!`; spacing and punctuation must match exactly.
1. Enter `list t/`.<br>
   Expected: The command is rejected with the tag constraint message; contacts are unchanged.
1. Close and relaunch the application.<br>
   Expected: Alex Lim still has the tags `Sec 4`, `Math/Science`, and `C++`. Enter `list r/student t/sec 4 t/c++` and verify that Alex appears.
1. Close TuitionBook. Open `data/tuitionbook.json` in the disposable folder, replace Alex's `tags` array with `[" Math ", "Math", " Sec  4 "]`, save the file, and relaunch TuitionBook.
1. Enter `list t/Math`.<br>
   Expected: Alex appears with one `Math` tag and one `Sec  4` tag. The padded and unpadded `Math` values normalize to the same tag, while its internal spacing is retained.
1. Enter `untag 1 t/Math`, then close TuitionBook and inspect Alex's tags in `data/tuitionbook.json`.<br>
   Expected: `Math` is removed. The remaining tag is stored as `Sec  4` without surrounding spaces.
1. Relaunch TuitionBook, enter `list`, then `edit 1 t/first t/second`.<br>
   Expected: Alex has two tags, `first` and `second`, not one tag containing `t/`. Field prefixes still delimit values.
1. Enter `edit 1 t/`.<br>
   Expected: All of Alex's tags are cleared, as before.

### Saving data

`LogicManager` uses `canMutate()` to snapshot contacts and exact working-set membership, order, predicate and refresh command before commands that can change data. After successful execution, it saves only when `requiresSave()` returns true; it does not compare the complete contact list. Each contact-changing command starts with a false save decision, resets it at the start of every execution, and sets it only after changing contact data. These commands override `canMutate()` to remain true independently of their latest save decision. Reusing a command after an earlier change therefore cannot cause a stale save request on a later no-change or rejected execution. Read-only commands and `reload` always return false from `requiresSave()`.

`link` requests saving only after creating or replacing a relationship; `unlink` only after removing one. `edit` compares the selected contact's stored values with its proposed replacement and requests saving only when they differ; the replacement preserves the persistent ID. An unchanged edit reports `No changes made: the specified values already match NAME.` and preserves the working set, as do other unchanged mutations. The comparison uses stored values, so spelling, capitalization and phone-format changes still count as edits; tag order does not. `clear` checks whether the complete book contains any contacts, including hidden contacts. Successful additions and deletions always request saving. Filter changes alone do not trigger saving. Actual changes retain the existing save-failure rollback.

**Performance:** The save decision no longer scans all contacts: it uses the command's mutation outcome, an emptiness check for `clear`, or a comparison of the selected contact for `edit`. Comparing that contact's fields and tags still depends on their sizes. Before executing a command for which `canMutate()` is true, `LogicManager` creates a rollback snapshot through `new TuitionBook(existingBook)` → `resetData` → `setPersons(List)` → `personsAreUnique`. For a valid book of n distinct contacts, this validation performs `n(n - 1) / 2` pairwise identity comparisons: 499,500 for 1,000 contacts. Each comparison normalizes and compares names, and also normalizes and compares phone numbers when the normalized names match. Snapshot creation therefore takes O(n²) time for fixed contact-field sizes, in addition to its O(n) reference copying; the comparison costs also depend on field lengths. The snapshot is created even when the command ultimately makes no change and skips saving. Avoiding unnecessary saves does not remove this cost or make overall command execution constant-time. No benchmark has established a user-visible speedup, and this complexity analysis alone does not establish whether the two-second performance NFR is met.

Run each scenario below independently in a fresh, disposable folder. An ordinary save failure rolls back the attempted change but does not set the storage write block: restore write access and repeat the failed command. A failed load whose backup also fails does set the write block: correct or move the file and use `reload` (or restart) before retrying contact-changing commands. If backing up an unreadable file succeeds, saving is already enabled and no reload is required. Reload reads the file; it does not save or replay a failed command.

1. Repeating commands without changing contact data

   1. In a fresh, disposable folder, enter `add r/guardian n/Grace Guardian p/90000001`, `add r/student n/Sam Student p/90000002 t/Math`, then `link 2 1`.
   1. Configure the data file or its folder so saving fails, then enter `edit 2 t/Science`.<br>
      Expected: A save error is reported and Sam's tag remains `Math`. This establishes the save-failure precondition; permission controls differ by operating system.
   1. Enter `link 2 1`, `edit 2 n/Sam Student`, and `edit 2 t/Math`.<br>
      Expected: Each command completes without a save error and contacts remain unchanged. The link command reports that the relationship already exists. Each edit reports `No changes made: the specified values already match Sam Student.` Repeating `edit 2 e/`, `edit 2 a/`, or `edit 2 t/Math t/Math` also reports no changes because those fields already match.
   1. Restore write access, then enter `edit 2 t/Science`, `unlink 2`, and `unlink 2` again.<br>
      Expected: The tag change and first unlink persist; the second unlink reports no linked guardian. Restart and verify that Sam has tag `Science` and no guardian link.

1. Routine manual editing with the app closed

   1. Launch TuitionBook in a fresh, disposable JAR folder. Enter `add r/student n/Alex Yeoh p/87438807`, then `find Nobody`, then `exit`.

   1. Copy `data/tuitionbook.json` to a separate backup location. Change only Alex's `name` to `Manually Edited Alex` in the original file, preserve the `id` and other fields, and save it as valid UTF-8 JSON.

   1. Relaunch TuitionBook.<br>
      Expected: the full list contains Manually Edited Alex despite the previous empty filter. The contact keeps its original ID and phone number, the edited file is not rewritten on startup, and the separate backup still contains Alex Yeoh.

   1. Enter `edit 1 n/Saved Alex`, then `exit` and relaunch.<br>
      Expected: Saved Alex is loaded from the data file; the separate backup remains unchanged.

1. Replacing contacts from a corrected file without restarting

   1. Launch TuitionBook with an empty, disposable data folder. Enter `add r/student n/Alex Yeoh p/87438807`, then `find Nobody`.

   1. Follow the UG's reload precautions: record any current contacts you need, copy `data/tuitionbook.json` to a separate safe location, and enter no contact-changing commands while editing the file. This is a controlled test of replacement; routine manual edits use the closed-app workflow above.

   1. While the app stays open, change only Alex's name in `data/tuitionbook.json` to `Reloaded Alex`, keeping the JSON valid, then save it. Enter `reload`.<br>
      Expected: the working set resets to the full loaded list containing Reloaded Alex, and the result reports one loaded contact. Alex Yeoh is replaced, not retained as a second contact. The contact ID is unchanged, reload does not rewrite the edited file, and the separate backup still contains Alex Yeoh.

   1. Replace the file with valid JSON containing a student whose `guardianId` refers to a missing contact, then enter `reload`.<br>
      Expected: the student remains loaded, the result identifies the affected contact by name and UUID, includes the invalid guardian ID and explains that the referenced contact is missing. The file remains unchanged until a later successful save.

   1. Enter `reload extra`.<br>
      Expected: invalid command format; contacts remain unchanged.

1. Reloading after the data file has been moved

   1. Launch TuitionBook in a fresh, disposable folder and enter `add r/student n/Alex Yeoh p/87438807`.

   1. Move `data/tuitionbook.json` to a separate backup location while the app stays open. Enter `reload`.<br>
      Expected: the contact list becomes empty and the result reports zero loaded contacts. The data file remains absent; reload does not recreate it.

   1. Enter `add r/student n/Alex Lim p/91234567`.<br>
      Expected: saving creates a new data file containing Alex Lim. The moved file remains unchanged.

1. Recovering when both loading and backing up the file failed

   1. In a fresh, disposable folder, add `add r/student n/Alex Yeoh p/87438807`, close TuitionBook, and keep a separate copy of the valid `data/tuitionbook.json`.

   1. Replace the data file's contents with `{broken`. Configure permissions so the app can read the file but cannot move it to a backup, then launch TuitionBook. Permission controls differ by operating system; verify that the startup warning explicitly reports backup failure before continuing.<br>
      Expected: the original file remains in place and saving is blocked. A successful backup means this scenario's precondition was not established.

   1. Enter `find Nobody`. While the app stays open, record any current contacts you need and copy the malformed file to a separate safe location. Enter no contact-changing commands during file recovery. Restore normal permissions and replace the malformed file with a copy of the saved valid file, keeping both separate copies. Enter `reload`.<br>
      Expected: the empty working set resets to the full loaded list containing Alex Yeoh, and saving is enabled. Reload does not rewrite the restored file or either separate copy. Merely correcting the file without reloading would leave the write block set.

   1. Enter `edit 1 n/Updated Alex`, then close and relaunch TuitionBook.<br>
      Expected: the edit saves successfully and Updated Alex remains visible after restart.

1. Recovering from an ordinary save failure (data loaded successfully)

   1. In a fresh, disposable folder, launch TuitionBook and enter `add r/student n/Alex Yeoh p/87438807`, then close the application.

   1. Remove your current user's write permission from the `data` folder beside the JAR, then relaunch TuitionBook.

   1. Enter `list r/student`, then `edit 1 n/Updated Alex`.<br>
      Expected: TuitionBook reports that it could not save. Alex Yeoh's name remains unchanged and the exact previous working set (membership, order and originating query) is restored.

   1. Restore write permission to the `data` folder and enter `edit 1 n/Updated Alex` again.<br>
      Expected: TuitionBook confirms the edit. Close and relaunch the application; the contact is named Updated Alex.

1. Retrying legacy-file migration after a permission failure

   1. In a fresh, disposable JAR folder, create `data/addressbook.json` containing `{"persons":[{"name":"Legacy Alex","phone":"87438807","tags":[]}]}` and ensure that `data/tuitionbook.json` is absent. Make the `data` folder readable but not writable for the current user; permission controls differ by operating system.<br>
      Expected: creating or renaming a file in `data` fails, while `addressbook.json` remains readable. If not, the permission-failure precondition was not established.

   1. Launch TuitionBook.<br>
      Expected: TuitionBook opens without an initialization error and displays Legacy Alex. `addressbook.json` remains unchanged and `tuitionbook.json` is absent.

   1. Enter `edit 1 n/Updated Alex`.<br>
      Expected: TuitionBook reports that it could not save and restores the contact as Legacy Alex.

   1. Close TuitionBook, restore write permission to `data`, and relaunch it.<br>
      Expected: TuitionBook displays Legacy Alex, moves the data to `data/tuitionbook.json`, and removes `data/addressbook.json`. Later contact changes can be saved normally.

1. Recovering from malformed contact data (backup succeeds)

   1. Place the JAR file in a fresh, disposable folder. Create a `data` folder beside the JAR file, then create `data/tuitionbook.json` containing exactly `{broken`. Keep a copy of this malformed file for comparison.

   1. Launch TuitionBook.<br>
      Expected: TuitionBook opens with an empty contact list and a warning. The original bytes are in `data/tuitionbook.json.backup` (or the next available numbered backup), and `data/tuitionbook.json` is absent.

   1. Enter `help`.<br>
      Expected: The help window opens and the result area displays `Opened help window.` No storage error is displayed.

   1. Close the help window, then enter `add r/student n/Alex Lim p/91234567`.<br>
      Expected: TuitionBook saves the new contact in `data/tuitionbook.json`. The backup file remains unchanged.

   1. Enter `exit`.<br>
      Expected: TuitionBook exits normally without displaying a storage error.

   1. Compare `data/tuitionbook.json.backup` with the saved copy.<br>
      Expected: The backup file is identical to the saved copy; TuitionBook did not modify its malformed contents.

1. Preserving ambiguous JSON input

   1. Place the JAR file in a fresh, disposable folder. Create `data/tuitionbook.json` containing exactly `{"persons":[]} {"persons":[{"name":"Hidden Student","phone":"81234567","tags":[]}]}` and launch TuitionBook.<br>
      Expected: TuitionBook rejects the trailing second JSON value, opens with an empty contact list, and moves the original file unchanged to `data/tuitionbook.json.backup` (or the next available numbered backup).

   1. Close TuitionBook and repeat in another fresh, disposable folder with `data/tuitionbook.json` containing exactly `{"persons":[],"persons":[{"name":"Hidden Student","phone":"81234567","tags":[]}]}`.<br>
      Expected: TuitionBook rejects the duplicate `persons` property, opens with an empty contact list, and moves the original file unchanged to `data/tuitionbook.json.backup` (or the next available numbered backup).

1. Handling UTF-8 encoding and read-permission failures

   1. In a fresh, disposable JAR folder, use an editor that can select encodings to create `data/tuitionbook.json` as UTF-8 with a BOM, containing exactly `{"persons":[]}`. Launch TuitionBook.<br>
      Expected: TuitionBook loads the empty contact list without a recovery warning. The data file remains at its original path and no `.backup` file is created.

   1. Close TuitionBook. In another fresh, disposable folder, create a valid contact whose address is `X`, then use a hex editor to replace the byte for that `X` with the standalone byte `80` (hexadecimal). Launch TuitionBook.<br>
      Expected: TuitionBook reports that the file is not valid UTF-8 JSON, moves the original bytes unchanged to an unused `.backup` path, and starts with an empty contact list. Comparing the backup in the hex editor shows the same invalid byte.

   1. On macOS or Linux, close TuitionBook and repeat in another disposable folder with valid `{"persons":[]}` data. Leave the `data` folder writable but remove the data file's read permission, for example with `chmod 000 data/tuitionbook.json`, then launch TuitionBook.<br>
      Expected: TuitionBook reports that it lacked read permission and moves the file unchanged to an unused backup. The warning tells the user to restore read permission and does not say that valid JSON must be fixed. Restore permission on the backup after the test.

1. Distinguishing guardian-link repair from an invalid JSON value type

   1. In a fresh, disposable folder, create a current-format student with valid `id`, `role`, name, and phone fields, but set `guardianId` to the JSON object `{"unexpected":"value"}`. Launch TuitionBook.<br>
      Expected: TuitionBook cannot deserialize the file, starts with an empty contact list, and preserves the complete file through the invalid-file recovery workflow. It does not report a repaired guardian link because object and array values are not supported link strings.

1. Warning about a repaired guardian link

   1. In a fresh, disposable folder, create a valid `data/tuitionbook.json` containing a student whose `guardianId` refers to a missing contact.

   1. Launch TuitionBook.<br>
      Expected: The valid contact remains visible, its invalid guardian link is removed, and a warning reports one repaired guardian link. The scrollable details area identifies the student by name and UUID, shows the invalid `guardianId`, and explains that the referenced guardian is missing.

   1. Close TuitionBook and repeat in another disposable folder with two or more students whose `guardianId` values are malformed, missing, self-referential, or refer to students.<br>
      Expected: All valid contacts remain visible, all invalid links are removed, and the scrollable warning lists every affected contact in JSON order with its repair reason.
