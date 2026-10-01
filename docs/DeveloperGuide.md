---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# AB-3 Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

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

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
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

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


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

* has a need to manage a significant number of contacts
* prefers desktop apps over other types of applications
* can type fast
* prefers typing to mouse interactions
* is reasonably comfortable using CLI apps

**Value proposition**: Manage tuition contacts efficiently through keyboard-entered commands.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …                                    | I want to …                 | So that I can…                                                        |
|----------|--------------------------------------------|------------------------------|------------------------------------------------------------------------|
| `* * *`  | private tutor | see usage instructions | refer to instructions when I forget how to use TuitionBook |
| `* * *`  | private tutor | add a student or guardian contact | keep contact details for tuition arrangements |
| `* * *`  | private tutor | edit a contact | correct or update its details without recreating it |
| `* * *`  | private tutor | link a guardian to a student | find the appropriate guardian when needed |
| `* * *`  | private tutor | view a student's guardian details | contact the guardian while retaining the student's context |
| `* * *`  | private tutor | delete a contact safely | remove obsolete contacts without leaving invalid guardian links |
| `* *`    | private tutor | find a contact by name | locate it without scanning the complete list |
| `* *`    | private tutor | filter contacts by role | focus on students or guardians |

*{More to be added}*

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

**Use case: UC03 Edit a contact**

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

**Use case: UC04 Find contacts by name**

**MSS**

1.  User submits a find request with one or more name keywords.
2.  TuitionBook displays the contacts matching the requested name keywords and reports the number of matches.

    Use case ends.

**Extensions**

* 1a. No name keyword is provided.

    * 1a1. TuitionBook shows an error message describing the invalid request.

      Use case ends.

**Use case: UC05 View contact details**

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

**Use case: UC06 Delete a contact**

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

**Use case: UC07 Filter contacts by role**

**MSS**

1.  Private tutor requests contacts with a specified role.
2.  TuitionBook displays contacts with that role and reports the number of matches.

    Use case ends.

**Extensions**

* 1a. The requested role is invalid.

    * 1a1. TuitionBook shows an error message describing the invalid request.

      Use case ends.

**Role-filter command rule**

The role-filtered form supersedes the plain-list rule: `list` displays all contacts and clears any active filter, while `list r/ROLE` displays only contacts with the specified role. Other trailing text is rejected.

### Non-Functional Requirements

1.  TuitionBook shall run without an application installer on any mainstream desktop operating system with Java `25` or later installed.
2.  TuitionBook shall support at least 1,000 contacts. On a reference workstation with a dual-core 1.6 GHz processor, 8 GB RAM, and Java `25`, each listing, finding, filtering, viewing, adding, editing, deleting, or linking command shall complete within two seconds for a data set of 1,000 contacts, measured from command submission to the result being displayed.
3.  All documented add, edit, delete, list, find, filter, view, and link operations shall be executable through keyboard-entered commands, without requiring a mouse.
4.  TuitionBook shall operate as a single-user desktop application and shall not require a network connection for its normal contact-management functions.
5.  TuitionBook shall store contact data only in a local, human-editable text file and shall not transmit contact data to an external service.
6.  Changes made by successful mutating operations shall be available after a normal application restart. This does not guarantee preservation of a change when saving it reports a storage error.
7.  TuitionBook shall recover without terminating unexpectedly when its local data file cannot be loaded cleanly: if the file is missing, it shall start with an empty data set and inform the user; if it is malformed, it shall retain a backup of the malformed file, start with an empty data set, and show a warning; if it contains an invalid guardian link, it shall load the valid contacts, omit the invalid link, and show a warning.

### Glossary

* **Contact**: A record in TuitionBook representing either a student or a guardian, with a name, phone number, and optional email address, home address, and tags.
* **Current contact list**: The contacts currently displayed after applying any active filter; indexes in index-based commands refer to this list.
* **Contact ID**: A unique, persistent identifier assigned by TuitionBook to a contact.
* **Data file**: The local text file that stores TuitionBook contacts and their recorded guardian links between application sessions.
* **Filter**: A condition that restricts the current contact list. TuitionBook supports a name filter or a role filter; applying a new filter replaces the previous one.
* **Guardian**: A contact with the `guardian` role. A guardian contact may have zero or more recorded student links; these links record information in TuitionBook and do not establish or determine real-world responsibility.
* **Guardian link**: A recorded association from a student to a guardian contact. A student has at most one guardian link, while a guardian can be linked to multiple students.
* **Home address**: A stored lesson or contact address, usually associated with a student; it can include details such as a unit number, gate code, or lift instructions.
* **Index**: A positive one-based position in the current contact list, used by index-based commands.
* **Mainstream OS**: Windows, macOS, or a commonly used Linux distribution.
* **Normalised name**: A name converted to its comparison form before duplicate checking: leading and trailing spaces are removed, each internal run of spaces is collapsed to one space, and names are compared case-insensitively while preserving display casing.
* **Normalised phone number**: A phone number converted to its comparison form before duplicate checking: spaces and hyphens are removed while a leading `+` is retained. Country-code equivalence is not applied in the MVP.
* **Private tutor**: A tutor who provides one-to-one tuition and travels to students' homes.
* **Role**: A contact classification. Users may enter `student` or `guardian` in any casing; TuitionBook stores and displays the corresponding `STUDENT` or `GUARDIAN` role.
* **Student**: A contact with the `student` role who may be linked to one guardian.
* **Tag**: An optional user-defined label attached to a contact for categorisation or filtering.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with an empty contact list. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

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

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
