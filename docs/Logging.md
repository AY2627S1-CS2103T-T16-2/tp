---
  layout: default.md
  title: "Logging guide"
---

# Logging guide

* The project uses the `java.util.logging` package for logging.
* `LogsCenter` manages logging levels and destinations.
* A class obtains a `Logger` with `LogsCenter.getLogger(Class)`, which logs messages at the configured level.
* Log messages are written to the console and rotating `.log` files.
* The output logging level can be controlled by changing the `LOG_LEVEL` constant in the `LogsCenter` class.
* **When choosing a level for a log message**, follow the conventions given in [_[se-edu/guides] Java: Logging conventions_](https://se-education.org/guides/conventions/java/logging.html).

## Log location and rotation

For a packaged JAR, TuitionBook stores diagnostic logs in the folder containing the JAR, regardless of the working directory from which it is launched. During development, when the application runs from a class directory instead of a packaged JAR, it stores logs in the process working directory.

TuitionBook keeps at most five rotating log files, each up to 5 MB. They are normally named `tuitionbook.log.0` through `tuitionbook.log.4`. Only files that have already been created are present.

## Contact storage and log handling

Contact storage and diagnostic logs are separate. `clear confirm` clears every contact, including contacts hidden by the current filter, but does not remove log files. Logs may contain information entered in earlier commands and their feedback, so review them before sharing them.

To remove the logs, first close TuitionBook, then delete every existing numbered log file from the applicable folder described above. Ordinary file deletion is not secure erasure, and TuitionBook does not provide a secure-erasure mechanism.
