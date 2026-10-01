[![CI Status](https://github.com/AY2627S1-CS2103T-T16-2/tp/workflows/Java%20CI/badge.svg)](https://github.com/AY2627S1-CS2103T-T16-2/tp/actions)
[![codecov](https://codecov.io/gh/AY2627S1-CS2103T-T16-2/tp/branch/master/graph/badge.svg)](https://codecov.io/gh/AY2627S1-CS2103T-T16-2/tp)

# TuitionBook

![Ui](docs/images/Ui.png)

**TuitionBook is a desktop address book for private tutors.** Every student comes with a guardian who pays and a home address to travel to. TuitionBook keeps student and guardian contacts linked and grouped by level and subject, so the tutor can reach the right person and find the right address quickly.

It is optimised for tutors who prefer typing: while it has a GUI, most interactions happen through a Command Line Interface (CLI), so frequent tasks can be done faster than with a mouse.

## Features

* Add, edit, find, list and delete contacts
* Mark each contact as a **student** or a **guardian**
* **Link** a guardian to a student (one guardian per student; a guardian may have several students, e.g. siblings)
* Look up a student and see their guardian's contact details in one step
* Link-aware deletion, so the data never points at a contact that no longer exists
* Data is saved automatically after every change and reloaded on startup

## Getting started

* Requires Java `25` or above installed on your computer.
* Download the latest `.jar` file from the [Releases](https://github.com/AY2627S1-CS2103T-T16-2/tp/releases) page and run it with `java -jar <filename>.jar`.
* See the [User Guide](https://ay2627s1-cs2103t-t16-2.github.io/tp/UserGuide.html) for a quick start and the full list of commands.

## Documentation

* [Product website](https://ay2627s1-cs2103t-t16-2.github.io/tp/)
* [User Guide](https://ay2627s1-cs2103t-t16-2.github.io/tp/UserGuide.html)
* [Developer Guide](https://ay2627s1-cs2103t-t16-2.github.io/tp/DeveloperGuide.html)
* [About Us](https://ay2627s1-cs2103t-t16-2.github.io/tp/AboutUs.html)

## Acknowledgements

This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).

Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5)
