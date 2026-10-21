---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# AB-3 Developer Guide

TuitionBook is scoped to English-language users and any `en-*` locale (for example, `en-SG`, `en-GB`, or `en-US`). Text input is encoded as UTF-8. Field-specific validation rules apply to names, addresses, phone numbers, email addresses, and tags.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

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

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T16-2/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-T16-2/tp/tree/master/src/main/java/seedu/tuitionbook/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

`PersonListPanel` receives both the filtered contact list and the complete contact list from `Logic`. It displays the filtered list while using the complete list to resolve a student's guardian name and contact number and a guardian's linked student names, including when a role filter hides the related contacts' cards. The panel refreshes its cards when the complete list changes so that relationship details remain current.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-T16-2/tp/tree/master/src/main/java/seedu/tuitionbook/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to a `TuitionBookParser` object, which creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the arguments. It creates `HelpCommand`, `ExitCommand`, and `ReloadCommand` directly instead.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. Each `Command` declares whether it requires contact data to be saved. After a command executes successfully, `LogicManager` saves the `Model`'s contact data only when the command requires it. If saving fails, `LogicManager` restores the contacts and active filter that existed before the command, then reports a command error.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" alt="Representative parser classes and their dependencies"/>

How the parsing works:
* `TuitionBookParser` creates a command-specific parser for `add`, `edit`, `tag`, `untag`, `delete`, `clear`, `find`, `list`, `link`, `unlink`, and `view`. Each implements `Parser<T extends Command>` for its command type. The diagram shows `AddCommandParser` and `TagCommandParser` as representative examples; the remaining command-specific parsers are omitted.
* `AddCommandParser` uses `ArgumentTokenizer`, `ArgumentMultimap`, the `Prefix` constants in `CliSyntax`, and `ParserUtil` to parse arguments and construct an `AddCommand`. `EditCommandParser`, `ListCommandParser`, and `ViewCommandParser` also use these utilities. Other parsers use only the helpers they need; for example, `DeleteCommandParser`, `LinkCommandParser`, and `UnlinkCommandParser` use `ParserUtil` to parse indexes.
* `TagCommandParser` and `UntagCommandParser` delegate argument parsing to the package-private `TagModificationCommandParser` helper, passing their respective command constructor references. The helper uses the same tokenization and validation utilities and invokes the supplied constructor after parsing. It does not implement `Parser`.
* `ArgumentTokenizer` creates an `ArgumentMultimap`, which stores argument values keyed by `Prefix`. `CliSyntax` defines the recognized prefix constants; `ParserUtil` parses values and indexes without depending on `Prefix`.
* `TuitionBookParser` creates `HelpCommand`, `ExitCommand`, and `ReloadCommand` directly, without a command-specific parser. In all cases it returns the resulting object as a `Command`.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-T16-2/tp/tree/master/src/main/java/seedu/tuitionbook/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores TuitionBook data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `TuitionBook`, and each `Person` references tags from that list. This lets `TuitionBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T16-2/tp/tree/master/src/main/java/seedu/tuitionbook/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both TuitionBook data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonTuitionBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

`ApplicationPaths` resolves application-managed files before file logging starts. Packaged execution uses the directory containing the executable JAR as the application home, independent of the process working directory. Development runs from compiled class directories use the process working directory as an explicit fallback. Contact data, legacy data migration, preferences, logs, and log rotation/lock files therefore share the same application home.

### Common classes

Classes used by multiple components are in the `seedu.tuitionbook.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Incremental tag changes

`tag INDEX t/TAG [t/TAG]...` adds tags by set union; `untag INDEX t/TAG [t/TAG]...` removes them by set difference. `add` continues to accept an initial tag set and `edit` continues to replace the whole set. Stored tag identity is case-sensitive, unlike the case-insensitive `list` filter. Duplicate arguments are deduplicated, existing tags are ignored on addition, and absent tags are ignored on removal.

`TagCommandParser` and `UntagCommandParser` delegate index and tag validation to `TagModificationCommandParser`. It uses `ArgumentTokenizer.tokenizeWithQuotedValues` with only `PREFIX_TAG`, protecting embedded `t/` text inside quoted tags and decoding the same quote and backslash escapes as `add`, `edit`, and `list`. Other field prefixes remain literal tag text. Every decoded tag is validated before constructing a command, so malformed quotes and invalid or blank tags reject the entire operation. See [Quoted name and tag parsing](#quoted-name-and-tag-parsing) for the shared quoting rules.

`TagCommand` and `UntagCommand` share execution through `TagModificationCommand`. It resolves the displayed index and modifies a disposable copy of the selected contact's tags. A changed set produces one replacement `Person` retaining its ID, role, relationship, and all other fields, then restores the full list. A no-change result preserves the filter and requests no save. `canMutate()` remains true before execution so `LogicManager` captures the rollback snapshot, while `requiresSave()` is reset each execution and becomes true only after an actual tag change. Existing storage and save-failure rollback apply without a schema change.

`TagCommand` compares requested tags with the original contact's tag set to report only newly added tags in its success message. `UntagCommand` uses the same comparison to report every absent tag, both when other tags are removed and when nothing changes. Shared feedback formatting sorts names and encloses them in double quotes, escaping literal quotes and backslashes for unambiguous display. This formatting does not change tag matching or command parsing. The difference is recomputed on each execution rather than retained as command state.

### Viewing contact relationships

`ViewCommandParser` parses `view INDEX [v/]` with the same positive-index rule as other index-based commands. The optional empty `v/` prefix selects full details for linked contacts; the parser rejects duplicate prefixes and values after `v/`.

`ViewCommand` selects the contact from `Model#getFilteredPersonList()`, then reads the full TuitionBook to resolve relationships by persistent contact ID. Thus, a filtered list determines the selected index but cannot hide a linked guardian or student from the result. The command does not change the model or its active filter.

Without `v/`, the command displays each linked contact as `NAME (CONTACT_NUMBER)`: the linked guardian for a student or linked students in address-book order for a guardian. With `v/`, it displays each linked contact's full details. It displays `No guardian linked.` or `No students linked.` when the selected contact has no relationship. Missing optional fields use `-`, and tags are sorted alphabetically.

`ViewCommand` returns a `CommandResult` containing a short feedback message and the formatted detail text. `MainWindow` shows that text in `DetailDisplay`, which replaces `PersonListPanel` in the main content area. Executing a command that has no detail text, including an invalid command, restores `PersonListPanel`. The list and its active filter remain in the model while the detail view is visible.

### Contact field validation

`Name` and `Address` are model value objects that enforce their constraints at construction. Command parsers trim surrounding whitespace before constructing these objects, but do not otherwise normalize or truncate accepted values. Names and addresses support UTF-8 text subject to their field-specific validation rules. Names contain at most 100 Unicode code points and addresses contain at most 200 Unicode code points.

Name comparison treats canonically equivalent Unicode forms, such as é and e followed by a combining acute accent, as equal. Contacts are duplicates only when both their normalized names and normalized phone numbers match.

`JsonAdaptedPerson` uses the same model validators when loading JSON, keeping add, edit, and storage validation consistent. An address remains optional, and `edit INDEX a/` stores no address rather than an empty `Address`. A data file containing an over-limit name or address is rejected through the invalid-data recovery path. If the configured file can be moved to an unused sibling backup, the backup preserves the original bytes and saving new data is allowed. If that backup move fails, the original file remains in place and saving stays blocked until a successful read.

`PersonListCard.fxml` gives the role badge its preferred minimum width while allowing the name label to shrink. Each student card has one relationship row for its guardian's name and contact number, and each guardian card uses the same row for linked student names in contact-list order. Long names, phone numbers, email addresses, relationship summaries, addresses, and tags retain their beginning and use an ellipsis within the card instead of widening it or pushing the badge out. `PersonCard` attaches a wrapped tooltip containing each full value; `view INDEX v/` also provides the full relationship details through the keyboard-driven workflow. Multiple tags continue to flow in alphabetical order.

### Quoted name and tag parsing

`AddCommandParser` and `EditCommandParser` opt into `ArgumentTokenizer.tokenizeWithQuotedValues` for `PREFIX_NAME` and `PREFIX_TAG`; `ListCommandParser` and the shared `TagModificationCommandParser` for `tag` and `untag` opt in for `PREFIX_TAG`. The tokenizer automatically recognises those quoted prefixes, visits prefix positions in order, and ignores positions inside a quoted value. It then removes the enclosing quotes before normal name or tag validation. It rejects unclosed quotes and trailing text outside the closing quote. Other fields retain their existing quote and prefix behaviour. The model and JSON store the plain name or tag, so no storage migration is needed. Unquoted prefix collisions remain ambiguous and retain their existing interpretation, as documented in the UG.

Inside quoted values, `\"` decodes to a literal double quote and `\\` to a literal backslash. Other backslash sequences and all unquoted values are preserved. The closing-quote scan and decoding use the same escape predicate, so an escaped quote cannot end a value or expose embedded prefixes. An escaped final quote without a subsequent closing quote is rejected. Surrounding whitespace is trimmed as before, and decoded values pass through the existing domain validators; escaping does not widen name validation.

Literal quotes already present in JSON are retained: CLI escaping is decoded before storage and does not change the JSON format or reinterpret stored strings. For example, a stored tag `"Sec 4"` can be filtered using `list t/"\"Sec 4\""`. This also covers data from intermediate development builds that treated enclosing quotes literally; no automatic stripping or migration of existing tags occurs.

### \[Historical proposal\] Undo/redo feature

Undo/redo is descoped from the final release under [scope decision 4.1](#decision-4-1-undo-and-confirmation). The following inherited design is retained only as a hypothetical implementation discussion: its classes, commands and model methods are not implemented, and it is not a release commitment.

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

* **Alternative 1 (current choice):** Saves the entire TuitionBook.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

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

<!-- Codex assisted ooixs with the language-scope and user-story status clarifications for PR #131. -->
TuitionBook is intended for English-language users and supports `en-*` operating-system locales. Its interface, documentation, and messages are in English; this does not require contact information to contain only English text. Contact fields accept UTF-8 text subject to their documented validation rules. In particular, names accept Unicode letters, numbers, and combining marks, with the accepted capitalization, punctuation, and Unicode representation preserved after surrounding whitespace is trimmed. Duplicate-contact checks treat canonically equivalent Unicode names as equal, as documented in the [User Guide](UserGuide.md#adding-a-contact-add).

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

US20 undo/redo is excluded from the final release. There is no command history that can restore a successful deletion or clear operation; users should back up the data file before bulk changes. General confirmation prompts are also excluded. The later [#141](https://github.com/AY2627S1-CS2103T-T16-2/tp/issues/141) decision adds a specific safeguard: `clear confirm` is required, and the message explicitly includes contacts hidden by filters. `delete INDEX` remains immediate. Save-failure rollback is implemented and restores contacts and the active filter after a failed save; it is distinct from user-invoked undo and does not reverse a successful command.

#### Help and notes

The earlier US12 descope was reversed by [#93](https://github.com/AY2627S1-CS2103T-T16-2/tp/issues/93): Help now includes an offline command summary, with browser access to the full UG and Copy URL as a fallback. It is a static syntax summary, not a context-sensitive help system.

US29 free-text notes remain excluded. Notes would need a deliberate syntax for text containing field-like prefixes; simply introducing another prefixed field could interpret part of a note as another argument. Name quoting under [#97](https://github.com/AY2627S1-CS2103T-T16-2/tp/issues/97) was extended to tags by [#163](https://github.com/AY2627S1-CS2103T-T16-2/tp/pull/163). Quoted names and tags support `\"` for a literal double quote and `\\` for a literal backslash. This protects embedded prefixes in those quoted fields; it does not add notes or quoting for every field. See [Quoted name and tag parsing](#quoted-name-and-tag-parsing).

#### Academic grouping and inactive contacts

US18/US19 needs are supported through generic tags: `t/Sec4 t/AMath` records a level and subject, and `list r/student t/Sec4 t/AMath` selects students with both labels. Dedicated academic fields and validation remain excluded. This updates the value proposition clarified in [#131](https://github.com/AY2627S1-CS2103T-T16-2/tp/pull/131).

`Active` and `Inactive` are ordinary organisational tags. They do not hide contacts, archive records, disable commands, or change guardian relationships. Tutors explicitly select `list r/student t/Active` when needed. Editing tags replaces the full set, so an `edit` changing status must retain every other desired tag, for example `edit 1 t/Inactive t/Sec4 t/AMath`. The later [#164](https://github.com/AY2627S1-CS2103T-T16-2/tp/issues/164) decision, implemented in [#166](https://github.com/AY2627S1-CS2103T-T16-2/tp/pull/166), adds `tag` and `untag` for incremental changes without retyping unrelated tags. These commands match stored tags case-sensitively and do not enforce mutually exclusive status labels. Dedicated archive/restore functionality remains excluded from the final release.

#### Tag identity and compatibility

[#155](https://github.com/AY2627S1-CS2103T-T16-2/tp/issues/155), implemented in [#160](https://github.com/AY2627S1-CS2103T-T16-2/tp/pull/160), deliberately broadens #152's single-hyphen rule to allow natural spaced labels and printable ASCII punctuation. Parsing trims surrounding whitespace, including inside quoted values, while internal spaces remain significant (`Sec 4` and `Sec  4` differ). Stored tag identity is case-sensitive; filtering compares complete tags case-insensitively. Under [#163](https://github.com/AY2627S1-CS2103T-T16-2/tp/pull/163), enclosing quotes delimit a value and are not stored; inside quoted values, `\"` decodes to a literal double quote and `\\` to a literal backslash. Unquoted prefix collisions retain their existing interpretation. Quoting the complete tag protects embedded prefixes in `add`, `edit`, `list`, `tag`, and `untag`; see the UG for examples.

Tag length is deliberately unbounded. The validator uses a linear scan, and cards truncate long values with an ellipsis while retaining the full text in a tooltip and detail view. This is not a guarantee of unlimited resource capacity. Once broader tags are saved, narrowing validation or normalising existing tag identities requires an explicit compatibility or migration plan.

#### Single-instance operation and manual file editing

Run only one TuitionBook instance against a data file. The application does not lock the file against another instance or an external editor; each save writes the current in-memory contacts and can overwrite another writer's changes. Atomic saving protects replacement of an individual file, not coordination between independent writers.

A lock-file guard was considered and deferred from v1.x because cross-platform locking and stale-lock recovery add risks in the final iteration. The supported manual-editing workflow is to close TuitionBook, back up and edit the file, then reopen it. The implemented `reload` command remains an explicit recovery option: it replaces in-memory contacts rather than merging concurrent changes. Single-instance enforcement is a deferred improvement, not an implemented safeguard or promised release feature.

#### Startup and unnecessary writes

US47's Week-5 sample-data status is superseded by [#109](https://github.com/AY2627S1-CS2103T-T16-2/tp/issues/109): a missing data file starts with an empty contact list, and a valid file containing an empty contact list stays empty. The application does not insert sample contacts on startup.

[#165](https://github.com/AY2627S1-CS2103T-T16-2/tp/pull/165) resolves unnecessary save attempts for repeated links, unchanged edits and clearing an empty contact list. Together with `unlink` and the incremental tag commands, contact-changing commands report their actual mutation through `requiresSave()`. Commands that leave contact data unchanged do not attempt a save; filter changes alone do not require saving. An unchanged `edit` still restores the full list, while an unchanged `link`, `unlink`, `tag` or `untag` preserves the filter. Rollback snapshots remain in place for commands that can mutate data, and actual changes retain normal save-failure rollback. Snapshot creation still copies O(n) contact references; no constant-time execution or measured speedup is claimed. See [Saving data](#saving-data) for the contract and manual checks.

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
2.  TuitionBook displays each contact whose name contains at least one requested substring, using Unicode-aware, case-insensitive literal matching, and reports the number of matches. Other contact fields and linked contact names are not searched.

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

**Use case: UC08 Filter contacts by role**

**MSS**

1.  Private tutor requests contacts with a specified role.
2.  TuitionBook displays contacts with that role and reports the number of matches.

    Use case ends.

**Extensions**

* 1a. The requested role is invalid.

    * 1a1. TuitionBook shows an error message describing the invalid request.

      Use case ends.

**Role-filter command rule**

The filtered form supersedes the plain-list rule: `list` displays all contacts and clears any active filter, while `list r/ROLE` and `list t/TAG...` display contacts matching the supplied criteria. Role matching and tag matching is exact and case-insensitive, and all supplied tags must be present. Other trailing text is rejected.

**Startup data rule**

If the configured data file is missing, TuitionBook starts with an empty contact list instead of creating sample contacts. An existing valid data file containing no contacts also remains empty after restart. This rule does not change the separate recovery behaviour for a malformed data file.

**Malformed-file recovery policy**

When the configured data file cannot be read, TuitionBook tries to move it unchanged to a sibling backup named `<data-file>.backup`. If that name is occupied, it tries `<data-file>.backup.1`, then increasing numeric suffixes. After a successful move, the load failure carries the exact backup path to the UI. TuitionBook starts with an empty contact list, can save new data at the configured path, and shows the exact backup path and recovery steps in both a warning dialog and the result display. If no backup can be created, it leaves the original file in place, blocks saving so that it cannot be overwritten, and tells the user to correct or move the original file before restarting.

### Non-Functional Requirements

1.  TuitionBook shall run without an application installer on any mainstream desktop operating system with Java `25` or later installed.
2.  TuitionBook shall support at least 1,000 contacts. On a reference workstation with a dual-core 1.6 GHz processor, 8 GB RAM, and Java `25`, each listing, finding, filtering, viewing, adding, editing, deleting, linking, or unlinking command shall complete within two seconds for a data set of 1,000 contacts, measured from command submission to the result being displayed.
3.  All documented add, edit, delete, list, find, filter, view, link, and unlink operations shall be executable through keyboard-entered commands, without requiring a mouse.
4.  TuitionBook shall operate as a single-user desktop application and shall not require a network connection for its normal contact-management functions.
5.  TuitionBook shall store contact data only in a local, human-editable text file and shall not transmit contact data to an external service.
6.  Changes made by successful mutating operations shall be available after a normal application restart. If saving a mutating operation reports a storage error, TuitionBook shall restore the contacts and active filter to their state before that operation.
7.  TuitionBook shall recover without terminating unexpectedly when its local data file cannot be loaded cleanly: if the file is missing, it shall start with an empty contact list and show an information dialog; if it is malformed and a backup succeeds, it shall retain a backup of the malformed file, start with an empty data set, show the exact backup path and recovery steps in a warning and result display, and allow saving; if that backup fails, it shall retain the original file, start with an empty data set, show that saving is blocked and how to remedy it, and block saving; if it contains an invalid guardian link, it shall load the valid contacts, omit the invalid link, and show a warning.

### Glossary

* **Contact**: A record in TuitionBook representing either a student or a guardian, with a name, phone number, and optional email address, home address, and tags.
* **Current contact list**: The contacts currently displayed after applying any active filter; indexes in index-based commands refer to this list.
* **Contact ID**: A unique, persistent identifier assigned by TuitionBook to a contact.
* **Data file**: The local text file that stores TuitionBook contacts and their recorded guardian links between application sessions.
* **Filter**: A condition that restricts the current contact list. TuitionBook supports a name filter or a combined role-and-tag filter; applying a new filter replaces the previous one. The role is optional, and all supplied tags must match exactly, ignoring case.
* **Guardian**: A contact with the `guardian` role. A guardian contact may have zero or more recorded student links; these links record information in TuitionBook and do not establish or determine real-world responsibility.
* **Guardian link**: A recorded association from a student to a guardian contact. A student has at most one guardian link, while a guardian can be linked to multiple students.
* **Home address**: A stored lesson or contact address, usually associated with a student; it can include details such as a unit number, gate code, or lift instructions.
* **Index**: A positive one-based position in the current contact list, used by index-based commands.
* **Mainstream OS**: Windows, macOS, or a commonly used Linux distribution.
* **Normalised name**: A UTF-8 name converted to its comparison form before duplicate checking: leading and trailing spaces are removed, each internal run of spaces is collapsed to one space, and names are compared case-insensitively while preserving display casing.
* **Normalised phone number**: A phone number converted to its comparison form before duplicate checking: spaces and hyphens are removed while a leading `+` is retained. Country-code equivalence is not applied in the MVP.
* **Private tutor**: A tutor who provides one-to-one tuition and travels to students' homes.
* **Role**: A contact classification. Users may enter `student` or `guardian` in any casing; TuitionBook stores and displays the corresponding `STUDENT` or `GUARDIAN` role.
* **Student**: A contact with the `student` role who may be linked to one guardian.
* **Tag**: An optional user-defined label attached to a contact for categorisation or filtering. Its name is non-blank printable ASCII (U+0020 to U+007E), including spaces and punctuation. `Tag` validates with a linear scan, without a length limit or recursive regular expression. Command parsing trims surrounding whitespace; internal spacing and spelling are preserved. Optional enclosing quotes protect recognised command prefixes and are removed before validation. JSON retains the plain tag string without migration. `Tag.equals` and `hashCode` remain case-sensitive; only list filtering uses exact case-insensitive comparison, requiring every requested tag and the optional role to match. `find` remains name-only.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on; testers are expected to do more *exploratory* testing.
</box>

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
1. Enter `clear`, `clear CONFIRM`, and `clear confirm extra` one at a time.<br>
   Expected: Each command reports the valid `clear confirm` format. Neither contact is deleted, and the student filter remains active.
1. Enter `clear confirm`.<br>
   Expected: All contacts are deleted, including Grace Guardian, who was hidden by the filter. The result states that contacts hidden by the current filter were also cleared.
1. Exit and restart TuitionBook.<br>
   Expected: The contact list remains empty.

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with an empty contact list and an information dialog explaining that no data file was found. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Getting help

1. Click the top-level `Help` control once.<br>
   Expected: The Help window opens immediately without a dropdown. It contains a scrollable summary covering `add`, `list`, `view`, `edit`, `link`, `unlink`, `find`, `delete`, `clear`, `help`, and `exit`. The Help window becomes active, so focus leaves the main TuitionBook window.
1. Minimize the Help window, focus the command box, and press <kbd>F1</kbd>.<br>
   Expected: The same Help window is restored and focused; no second Help window appears.
1. Close the Help window. In the main TuitionBook window, focus the command box and enter `help`. The Help window becomes active. Return to the main TuitionBook window, focus its command box, and enter `help` again.<br>
   Expected: The first command opens the Help window and reports `Opened help window.` The second command focuses the same window.
1. Click `Open User Guide`.<br>
   Expected: The User Guide opens in the system default browser. If the desktop environment cannot open a browser, TuitionBook remains open, displays fallback guidance, and leaves `Copy URL` available.
1. Click `Copy URL`, paste into a text editor, and confirm that the copied value is `https://ay2627s1-cs2103t-t16-2.github.io/tp/UserGuide.html`.
1. With the Help window open, activate the main TuitionBook window and select its `File` → `Exit`.<br>
   Expected: Both the main window and Help window close.

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

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

1. `add r/student n/Alex Yeoh p/87438807`
1. `add r/guardian n/Bernice Yu p/99272758`
1. `link 1 2`
1. `add r/student n/Charlotte Oliveiro p/93210283`
1. `add r/guardian n/David Li p/91031282`

1. Enter `view 1`.<br>
   Expected: The contact list is replaced by Alex Yeoh's details and `Bernice Yu (99272758)`. The result area displays `Viewing contact: Alex Yeoh`.
1. Enter `view 1 v/`.<br>
   Expected: Bernice Yu's full guardian details appear after Alex Yeoh's details.
1. Enter `view 2`.<br>
   Expected: The detail view shows Bernice Yu's details, followed by `Alex Yeoh (87438807)` under linked students.
1. Enter `view 2 v/`.<br>
   Expected: Alex Yeoh's full contact details appear under linked students.
1. Enter `view 3`, then `view 4`.<br>
   Expected: The unlinked student reports `No guardian linked.` and the guardian without students reports `No students linked.`
1. Enter `list r/student`, then `view 1 v/`.<br>
   Expected: Bernice Yu's details still appear. Enter `help`, close the help window, and confirm that the restored list remains filtered to students. Note that `add`, `edit`, `link`, and `unlink` clear the active filter, while `list` and `find` replace it, so they are unsuitable for this check.
1. Enter `view 0`, `view 99`, `view`, `view 1 extra`, `view 1 v/details`, and `view 1 v/ v/`.<br>
   Expected: Each command restores the contact list and reports an index or format error; no contact data changes.

### Finding contacts by name substring

Use a fresh, disposable folder and start the application with an empty contact list. Do not use real contact data. Enter these commands to create the contacts used below:

1. `add r/guardian n/Grace Tan p/91230001 e/parent@example.com a/Needle Street t/Family`
1. `add r/student n/Bob Lim p/91230002 e/student@example.com a/Example Road t/Math`
1. `link 2 1`
1. `add r/student n/A.B Tan p/91230003`
1. `add r/student n/Işık p/91230004`

1. Enter `find gr`, `find rac`, `find ace`, and `find GRACE` one at a time.<br>
   Expected: Each command shows only Grace Tan and reports `1 person(s) listed!`.
1. Enter `find tan`.<br>
   Expected: Grace Tan and A.B Tan are shown in their original order and the result reports `2 person(s) listed!`.
1. Enter `find rac bob`.<br>
   Expected: Grace Tan and Bob Lim are shown once each in their original order and the result reports `2 person(s) listed!`.
1. Enter `find ışık`, then `find ışı`.<br>
   Expected: Each command shows only Işık and reports `1 person(s) listed!`; matching is Unicode-aware and case-insensitive, including for a partial name.
1. Enter `find .`, then `find .*`.<br>
   Expected: The first command shows only A.B Tan. The second command shows no contacts because `.*` is matched literally rather than interpreted as a pattern.
1. Enter `find 91230002`, `find student@example.com`, `find Road`, `find Math`, and `find student` one at a time.<br>
   Expected: Each command shows no contacts and reports `0 person(s) listed!`; phone, email, address, tag, and role fields are not searched.
1. Enter `find grace`.<br>
   Expected: Only Grace Tan is shown; Bob Lim is not matched through the guardian link.
1. Enter `find bob`, then `list`.<br>
   Expected: `find bob` replaces the previous filter and shows only Bob Lim. `list` restores all five contacts in their original order. None of the contacts or the guardian link has changed.
1. Enter `find`, then enter `find` followed only by spaces.<br>
   Expected: Each command reports an invalid-command format message containing the updated `find` usage, including `Parameters: KEYWORD [MORE_KEYWORDS]...` and `Example: find rac bob`, and does not change the displayed list.

### Removing a guardian link

Use a fresh, disposable folder and start the application with an empty contact list. Do not use real contact data.

1. Enter `add r/student n/Alex Yeoh p/87438807`, `add r/guardian n/Bernice Yu p/99272758`, and `link 1 2`.<br>
   Expected: Alex Yeoh is linked to Bernice Yu.
1. Enter `list r/student`, then `unlink 1`.<br>
   Expected: The result area displays `Unlinked student Alex Yeoh from guardian Bernice Yu.` The full contact list is restored, both contacts remain, and Alex Yeoh has no guardian link.
1. Enter `list r/student`, then `unlink 1` again.<br>
   Expected: The result area displays `Student Alex Yeoh has no linked guardian.` The list remains filtered to Alex Yeoh; neither contact changes.
1. Close and relaunch the application, then enter `view 1`.<br>
   Expected: Alex Yeoh's details report `No guardian linked.`, confirming that the removed link remains absent after restart.

### Adding and removing individual tags

Use an empty contact list in disposable storage.

1. Enter `add r/guardian n/Grace Guardian p/90000001`, `add r/student n/Sam Student p/90000002 t/Math t/Sec 4`, and `link 2 1`.
1. Enter `find Sam`, then `tag 1 t/Science t/Math`.<br>
   Expected: Feedback is `Added tags to Sam Student: "Science".` Sam has `Math`, `Sec 4`, and `Science`. Grace and Sam are both displayed; Sam's ID, phone, and guardian link are unchanged.
1. Enter `list r/student`, then `untag 1 t/Math t/absent`.<br>
   Expected: Sam has only `Sec 4` and `Science`; feedback includes `Tags not present: "absent".` and the full list returns.
1. Enter `find Sam`, then `tag 1 t/Science` and `untag 1 t/science`.<br>
   Expected: Both commands report no change, only Sam stays displayed, and no save is attempted. The differently cased `science` does not remove `Science`; `untag` feedback includes `Tags not present: "science".`
1. Enter `tag 1 t/New t/` and `untag 1 t/Science t/`.<br>
   Expected: Both commands report tag constraint errors; no tags are added or removed.
1. Restart, then enter `view 2`.<br>
   Expected: Sam still has `Sec 4` and `Science` and remains linked to Grace.
1. Enter `edit 2 t/Only`, then `untag 2 t/Only`.<br>
   Expected: `edit` replaces the entire tag set with `Only`; `untag` then leaves no tags.
1. Enter `tag 2 t/"study t/weekly" t/"\"Sec 4\"" t/"C:\\Math\\"`, then restart.<br>
   Expected: Sam has exactly three tags: `study t/weekly`, `"Sec 4"` (including literal quotes), and `C:\Math\` (including the final backslash). His other details and guardian link are unchanged.
1. Enter `list t/"study t/weekly"`, then `untag 1 t/"study t/weekly" t/"\"Sec 4\"" t/"C:\\Math\\"`, then restart.<br>
   Expected: The filtered list initially contains only Sam. Removing the tags restores the full list, and Sam has no tags after restart.
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
1. Enter `list`, then `edit 1 t/first t/second`.<br>
   Expected: Alex has two tags, `first` and `second`, not one tag containing `t/`. Field prefixes still delimit values.
1. Enter `edit 1 t/`.<br>
   Expected: All of Alex's tags are cleared, as before.

### Saving data

`LogicManager` uses `canMutate()` to snapshot contacts and the active filter before commands that can change data. After successful execution, it saves only when `requiresSave()` returns true; it does not compare the complete contact list. Each contact-changing command starts with a false save decision, resets it at the start of every execution, and sets it only after changing contact data. These commands override `canMutate()` to remain true independently of their latest save decision. Reusing a command after an earlier change therefore cannot cause a stale save request on a later no-change or rejected execution. Read-only commands and `reload` always return false from `requiresSave()`.

`link` requests saving only after creating or replacing a relationship; `unlink` only after removing one. `edit` compares the selected contact's stored values with its proposed replacement and requests saving only when they differ; the replacement preserves the persistent ID. An unchanged edit still restores the full displayed list, as documented in the UG. `clear` checks whether the complete book contains any contacts, including hidden contacts. Successful additions and deletions always request saving. Filter changes alone do not trigger saving. Actual changes retain the existing save-failure rollback.

**Performance:** The save decision no longer scans all contacts: it uses the command's mutation outcome, an emptiness check for `clear`, or a comparison of the selected contact for `edit`. Comparing that contact's fields and tags still depends on their sizes. Creating the rollback snapshot still copies O(n) contact references; this change does not make overall command execution constant-time. No benchmark has established a user-visible speedup.

Run each scenario below independently in a fresh, disposable folder. An ordinary save failure rolls back the attempted change but does not set the storage write block: restore write access and repeat the failed command. A failed load whose backup also fails does set the write block: correct or move the file and use `reload` (or restart) before retrying contact-changing commands. If backing up an unreadable file succeeds, saving is already enabled and no reload is required. Reload reads the file; it does not save or replay a failed command.

1. Repeating commands without changing contact data

   1. In a fresh, disposable folder, enter `add r/guardian n/Grace Guardian p/90000001`, `add r/student n/Sam Student p/90000002 t/Math`, then `link 2 1`.
   1. Configure the data file or its folder so saving fails, then enter `edit 2 t/Science`.<br>
      Expected: A save error is reported and Sam's tag remains `Math`. This establishes the save-failure precondition; permission controls differ by operating system.
   1. Enter `link 2 1`, `edit 2 n/Sam Student`, and `edit 2 t/Math`.<br>
      Expected: Each command completes without a save error and contacts remain unchanged. The link command reports that the relationship already exists.
   1. Restore write access, then enter `edit 2 t/Science`, `unlink 2`, and `unlink 2` again.<br>
      Expected: The tag change and first unlink persist; the second unlink reports no linked guardian. Restart and verify that Sam has tag `Science` and no guardian link.

1. Reloading corrected data without restarting

   1. Launch TuitionBook with an empty, disposable data folder. Enter `add r/student n/Alex Yeoh p/87438807`, then `find Nobody`.

   1. While the app stays open, change Alex's name in `data/tuitionbook.json` to `Reloaded Alex`, keeping the JSON valid. Enter `reload`.<br>
      Expected: the list contains Reloaded Alex, the filter is cleared, and the result reports one loaded contact. Reload does not rewrite the file.

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

   1. While the app stays open, restore normal permissions and replace the malformed file with the saved valid copy. Enter `reload`.<br>
      Expected: Alex Yeoh appears, the filter is cleared, and saving is enabled. Merely correcting the file without reloading would leave the write block set.

   1. Enter `edit 1 n/Updated Alex`, then close and relaunch TuitionBook.<br>
      Expected: the edit saves successfully and Updated Alex remains visible after restart.

1. Recovering from an ordinary save failure (data loaded successfully)

   1. In a fresh, disposable folder, launch TuitionBook and enter `add r/student n/Alex Yeoh p/87438807`, then close the application.

   1. Remove your current user's write permission from the `data` folder beside the JAR, then relaunch TuitionBook.

   1. Enter `list r/student`, then `edit 1 n/Updated Alex`.<br>
      Expected: TuitionBook reports that it could not save. Alex Yeoh's name remains unchanged and the displayed list remains filtered to students.

   1. Restore write permission to the `data` folder and enter `edit 1 n/Updated Alex` again.<br>
      Expected: TuitionBook confirms the edit. Close and relaunch the application; the contact is named Updated Alex.

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

1. Warning about a repaired guardian link

   1. In a fresh, disposable folder, create a valid `data/tuitionbook.json` containing a student whose `guardianId` refers to a missing contact.

   1. Launch TuitionBook.<br>
      Expected: The valid contact remains visible, its invalid guardian link is removed, and a warning reports one repaired guardian link.
