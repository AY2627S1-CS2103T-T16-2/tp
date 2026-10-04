# AGENTS.md — Working on TuitionBook (CS2103T tP)

Instructions for AI coding agents (and humans) contributing to this repository.

**Context**: This is TuitionBook, a CS2103T team project (AY26/27 S1, team
`AY2627S1-CS2103T-T16-2`), evolved from AddressBook-Level3 (AB3). The released
product (v1.6), the published User Guide (UG), and the published Developer
Guide (DG) will be adversarially tested by peer testers during the course
**Practical Exam (PE)**. Every accepted bug costs marks. The team's goal is
**zero bugs caught in the PE**. Treat every change through the lens of
"how would a PE tester attack this?".

Authoritative sources (read these if in doubt — they override this file):

* PE rules and bug triaging guidelines: <https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-pe.html>
* Grading criteria incl. UG/DG bug lists: <https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html>
* Project constraints: <https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html>
* Deliverables spec (UG/DG requirements): <https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-deliverables.html>
* Course textbook (definitions): <https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/index.html>
* Java coding standard: <https://se-education.org/guides/conventions/java/intermediate.html>

---

## 1. What PE testers can and cannot report (scope)

In scope (keep these three consistent with each other at all times):

1. Behavior of the product JAR (`java -jar`, Java 25, on Windows/Linux/macOS).
2. The published UG page (source: `docs/UserGuide.md`).
3. The published DG page (source: `docs/DeveloperGuide.md`).

Not in scope: `README.md` and other repo files, data/config files shipped with
the app (unless they affect app behavior), terminal/console output (unless it
alarms or misleads a user who glances at it), and code quality (graded
separately, not PE-reportable). Several of these are still *graded* by tutors
and scripts — see section 10.

Key facts that shape how we work:

* **Bugs inherited from AB3 still count.** Do not assume upstream behavior is
  safe. If you touch a feature, you own its AB3 legacy bugs too.
* **Listing something under "Known issues" in the UG only reduces severity;
  it does not make it immune.** Fix it if feasible.
* **Items in the DG appendix "Planned Enhancements" are immune** from
  `type.FeatureFlaw` reports (see section 7). Use that appendix deliberately
  for flaws we cannot fix in time.
* Typos are always reportable (`severity.VeryLow`, `type.DocumentationBug`),
  even typos inside the app's UI, and can never be dismissed as not-in-scope.

## 2. Golden rules (apply to every PR)

1. **Product, UG, and DG must always agree.** Any mismatch is a guaranteed
   PE bug: if the product is wrong it is a `FunctionalityBug`/`FeatureFlaw`;
   if the doc is wrong it is a `DocumentationBug`. When you change behavior,
   update in the same PR: `docs/UserGuide.md`, the DG (use cases, user
   stories, NFRs, glossary, "Instructions for manual testing" appendix,
   diagrams in `docs/diagrams/*.puml`), and the in-app help/error messages.
2. **No input may ever crash the app, corrupt the data file, or make the app
   unusable.** Plausible user mistakes (e.g., a missing space between
   parameters, a huge number where an index is expected, pasted multi-line
   text) must produce a clear error message, not an exception or wrong
   behavior. Only deliberate-sabotage-only problems (e.g., a 30-digit phone
   number *that no user mistake could plausibly produce*) are excused — and
   even then, graceful rejection is required if the user can attempt it.
3. **Every error message must be specific and correct.** State which
   parameter/field is wrong, why, and what a valid value looks like. A correct
   but vague message ("Invalid input") is a reportable `FeatureFlaw`. A
   message that misidentifies the problem (e.g., calling an invalid value a
   "format error") is a reportable bug as well: a *format* error means the
   shape is wrong (`2021-13-28` for `YYYY-MM-DD`); an *invalid value* has the
   right shape but an impossible value (`2021-02-30`).
4. **Never let WIP features leak into a release.** A discoverable,
   undocumented, half-working feature is a reportable bug. Hide, disable, or
   finish it (and document it in the UG) before release.
5. **Prefer warning over blocking** for unusual-but-possibly-legitimate input;
   prefer blocking (with a specific error) for harmful input. See section 5.

## 3. Hard project constraints (violations = PE-reportable FeatureFlaws)

Do not introduce anything that violates these:

* **Typing-preferred**: all features must be operable via CLI-style commands,
  optimized for fast typists. GUI-only features are a flaw.
* **Single-user, local**: no multi-user features, no login, no shared storage.
* **Human-editable data file**: data stays in a local, human-editable text
  file (currently JSON at `[JAR location]/data/addressbook.json`). Support for
  manual edits must be **at least AB3's level**: a correctly edited file loads
  fine; an incorrectly edited file must never crash the app or silently
  destroy data beyond what the UG warns about. Do not change the format to
  something unsuitable for hand-editing. Do not use a DBMS.
* **OO paradigm** primarily, in Java.
* **Platform-independent**: must work on Windows, Linux, macOS. No
  OS-specific libraries, paths, or features (watch for path separators, font
  assumptions, `Desktop`/shell integrations).
* **Java 25 only**: must run on a machine with only Java 25 installed. Do not
  raise or lower the toolchain, and do not use preview features.
* **Portable**: runs via `java -jar` with no installer, no admin rights.
* **No remote server / minimal network**: the app must be fully usable
  offline. Third-party libraries need prior teaching-team approval, must be
  free/open-source with permissive licenses, and must not require user
  installation or account creation.
* **GUI resolution**: must *work well* at 1920x1080 and higher at 100% and
  125% scale, and remain *usable* at 1280x720 and at 150% scale. Test window
  resizing: nothing should become unreachable, overlap, or get cut off in a
  way that hinders use.
* **Single JAR ≤ 100MB**: do not add heavy assets/dependencies; do not bloat
  the JAR.

## 4. Avoiding `type.FunctionalityBug` (code-level checklist)

A functionality bug = behavior differs from the UG, a legitimate user action
is unhandled, or unspecified behavior differs from reasonable expectations.

For every command you add or touch, verify (and add JUnit tests for) at least:

* Missing, duplicated, unknown, and out-of-order parameters/prefixes.
* Empty values (`n/`), whitespace-only values, leading/trailing spaces.
* Index edge cases: `0`, negative, non-numeric, larger than list size, and
  values beyond `Integer.MAX_VALUE` (overflow must yield a proper "invalid
  index" style error, not a confusing or incorrect one).
* Very long values (names, addresses, tags, numbers): the GUI must not break.
  Text should wrap or truncate in a way that still lets the user see what they
  need; losing the *start* of a value or breaking the layout escalates
  severity. Reasonable length limits are acceptable only if justified from the
  user's perspective and enforced with a clear error message.
* Special characters users realistically need: names like `s/o`, `d/o`,
  hyphens, apostrophes (`O'Brien`), accented letters. If a character must be
  disallowed (e.g., it clashes with the command syntax), the error message and
  UG must say so, and the restriction must not block realistic legal names —
  otherwise it is a reportable flaw.
* Commands documented as taking no parameters (`help`, `exit`, `clear`,
  `list` without args) must behave exactly as the UG states for extraneous
  input (current UG: extraneous parameters are ignored for `help`/`exit`/
  `clear`; `list` rejects unknown trailing text). Keep code and UG aligned.
* Interactions between filtering (`find`, `list r/ROLE`) and index-based
  commands (`edit`, `delete`, `view`, `link`): indexes always refer to the
  *currently displayed* list; confirm messages and links behave correctly
  after any filter.
* Linked-data invariants: deleting or editing a student/guardian must never
  leave a dangling guardian link (see DG guarantees UC02/UC06); saving and
  reloading must preserve roles and links exactly.
* Data file handling: manually edit `data/addressbook.json` with (a) valid
  changes — they must load; (b) structurally broken JSON; (c) valid JSON with
  invalid values or an invalid/dangling link. Behavior must match the UG and
  DG NFRs *exactly* (including whether a backup of the bad file is kept and
  when it gets overwritten). The app must never crash on load.
* GUI state: multi-monitor/off-screen recovery, minimized help window, and
  anything listed in UG "Known issues" — do not regress these, and fix them
  when the feature-freeze budget allows.
* Console/terminal output: no stack traces or alarming warnings during normal
  operation. Use the project's logging (`java.util.logging` via `LogsCenter`)
  instead of `System.out`/`printStackTrace`.

Defensive coding expectations (also graded under code quality): use
assertions for internal invariants, exceptions for error paths, logging at
appropriate levels, and never swallow exceptions silently.

## 5. Avoiding `type.FeatureFlaw` (design-level checklist)

A feature flaw = a delivered feature is less useful than it reasonably should
be for the target user (private home tutors who type fast), or a design/
constraint violation. Checklist:

* **Case sensitivity must mirror the real world.** Person names, search
  keywords, role values (`student`/`STUDENT`), and command keywords where
  practical should be case-insensitive. Incorrect case sensitivity is a
  reportable flaw.
* **Duplicate detection must not be naive.** Exact-string-only matching is a
  flaw: `John Doe` vs `john doe`, or doubled internal spaces, are likely the
  same person. This project defines normalised-name + normalised-phone
  comparison (see DG glossary) — keep implementation, UG description, and
  error/warning messages consistent with that definition, and make the
  *limitations* of duplicate detection clear to the user. Prefer warning on
  near-matches over silently allowing or hard-blocking.
* **No overzealous validation.** Do not reject input merely because it is
  unusual: e.g., multiple numbers in a phone field (`1234 5678 (HP)`),
  past dates for record-keeping, unusual-but-real addresses. Block only when
  accepting would genuinely harm operation; otherwise accept (optionally with
  a warning). Equally, *failing to handle* harmful input is a flaw — there
  must always be either blocking or a warning for dangerous input.
* **Search should be forgiving**: case-insensitive, OR-semantics across
  keywords (current `find` behavior — do not regress to AND), and document
  exactly what is matched (full word vs partial).
* **Command formats must be fast to type**: short keywords, no unnecessary
  case-sensitivity, no hard-to-type special characters. If a long keyword is
  needed for clarity, consider also accepting a short alias. Unnecessarily
  complicated formats are reportable flaws.
* **Error messages**: see Golden rule 3 — vague or over-broad messages are
  feature flaws.
* **Don't build hard-to-test features** (remote APIs, audio, timing-dependent
  UI, colors as the only signal): testability is graded, and such features
  invite bug reports.
* **Missing-but-essential functionality is reportable** even if undocumented.
  If a v1.6 feature is knowingly incomplete, either gate it out of the release
  or cover the gap in "Planned Enhancements" (section 7).

## 6. Avoiding UG `type.DocumentationBug`

The UG is judged as a user-facing document for the target user. Reportable UG
bugs (if they hinder the reader) include:

* Any statement that does not match actual product behavior — the #1 source
  of PE bugs. After any behavior change, re-verify every affected UG sentence,
  example, and screenshot.
* Broken or wrong links; wrong command summaries; examples that fail when
  copy-pasted into the app (test every example literally, in order, on a
  fresh data set — note some examples assume prior state like `list` first).
* Typos and grammar errors (always reportable; typos can never be rejected).
* Screenshots that are outdated, insufficient, poorly integrated, or
  needlessly repetitive. Update screenshots whenever the GUI changes; crop to
  the relevant region where possible.
* Missing coverage: every current user-visible feature must be documented.
  Unreleased/future features must be marked `[coming in vX.Y]` / "Coming
  soon". Fine detail may be omitted *only if* the app itself informs the user
  (e.g., via a specific error message).
* Unclear target user/value proposition, messy formatting, inconsistent
  terminology (use the DG glossary terms consistently — e.g., the UG should
  consistently say "TuitionBook", not leftover "AddressBook" references).
* Keep PDF conversion in mind (graders/testers use a PDF copy): avoid layouts
  that split tables/diagrams awkwardly across pages, and avoid constructs
  that break when copy-pasting commands from PDF (the UG already warns about
  multi-line commands; keep examples on one line where possible).

## 7. Avoiding DG `type.DocumentationBug`

All UG rules above apply to the DG too (reader = a new developer). In
addition:

* **UML correctness is strictly checked.** Use only notation taught in the
  course (see the UML reference sheet linked from the course site). Common
  reportable errors: wrong arrowheads, dashed vs solid line mix-ups
  (association vs dependency; return arrows), missing multiplicities,
  class/object notation confusion, inconsistent omission of activation bars
  (omit everywhere or nowhere), diagrams contradicting the actual code. Edit
  the `.puml` sources in `docs/diagrams/`, regenerate, and verify the
  rendered image matches the code *as merged*.
* **Keep diagrams simple.** A sequence diagram should show internals of at
  most one component (treat others as black boxes). Architecture-level
  diagrams must stay high-level: no lower-level details, no indiscriminate
  double-headed arrows. Overly small/dense diagram text is reportable.
  Deliberate omissions are fine; note them for the reader when useful.
* **User stories** must follow `As a {role} I can {function} so that
  {benefit}`, with the three parts present and mutually consistent, and no
  important user story missing.
* **Use cases**: correct format (MSS + extensions, numbering like `1a.`,
  `1a1.`), no UI-level details (say "User requests to delete a contact", not
  "User clicks..."), no missing steps or missing extensions, step numbering
  correct, and behavior matching the implemented product.
* **NFRs** (definition from the course textbook — *Requirements* chapter):
  functional requirements specify **what the system should do**;
  non-functional requirements specify **the constraints under which the
  system is developed and operated** (performance, environment, usability,
  data size, process, etc.). Each NFR must be:
  * genuinely non-functional (not a feature in disguise),
  * clearly scoped and verifiable (measurable conditions, like the existing
    "1,000 contacts / 2 seconds on reference hardware" NFR),
  * reasonably achievable, and
  * **actually satisfied by the product** — an unmet reasonable NFR is
    reportable as a `FeatureFlaw`; an unreasonable NFR is itself a
    `DocumentationBug`. Do not add aspirational NFRs the product cannot meet,
    and re-check listed NFRs before each release.
  * Highly relevant missing NFRs are also reportable — keep the list honest
    and complete.
* **Glossary**: include important domain terms (student, guardian, guardian
  link, normalised name/phone, etc.); exclude terms needing no explanation.
* **Appendix: Instructions for manual testing**: must cover all user-testable
  features (AB3 leftovers we did not touch are exempt), must *complement* the
  UG rather than repeat it, and **every instruction and expected result must
  be accurate** — inaccurate test instructions are reportable bugs. Replace
  any leftover `{ more test cases ... }` placeholders before v1.6.
* **Appendix: Planned Enhancements** (add only after v1.5):
  * Max `team_size x 2` items; state the team size on the first line; use a
    numbered list.
  * Each item = exactly **one** specific enhancement (bundling two fixes into
    one item is itself a reportable DG bug), must be a *tweak to an existing
    feature* (not a new feature), and must describe the flaw it addresses and
    the exact intended behavior (sample inputs/outputs where applicable).
    Specific-fix phrasing, e.g. "Make the error message for a failed deletion
    also state the reason", not vague "improve error messages".
  * Items beyond the allowed count get no immunity, and exceeding the count
    is itself reportable.
* **Acknowledgements section** must credit *all* reuse: libraries, code from
  the internet, other projects (including past tPs), and AI-generated
  code/content, per the course reuse policy. Uncredited reuse risks
  plagiarism proceedings — far worse than any PE bug.
* **Appendix: Requirements** must be kept in sync with what is actually
  implemented (use case steps match real behavior; implemented vs future
  requirements clearly distinguishable).
* Proposed/future implementation sections (e.g., Undo/redo) are allowed to
  stay, but anything presented as *current* must match the code.

## 8. Bug severity/type vocabulary (for prioritization decisions)

* `type.FunctionalityBug`: behavior differs from spec/expectation.
* `type.FeatureFlaw`: feature missing/incomplete/suboptimally designed for
  the target user; includes constraint violations and low testability.
* `type.DocumentationBug`: flaw in UG/DG content.
* `severity.VeryLow`: purely cosmetic (typo, spacing, color).
* `severity.Low`: unlikely to affect normal operation; rare, minor
  inconvenience.
* `severity.Medium`: occasional inconvenience to some users.
* `severity.High`: makes the product almost unusable for most users.

Obvious, highly visible problems also damage credibility and get pushed to
higher severity (e.g., UML notation errors are `Low`+, broken links
`Low`-`Medium`). Prioritize fixes accordingly: crash/data-loss paths first,
then wrong behavior, then misleading docs, then cosmetics.

## 9. Feature freeze (iteration v1.6) — compliance warning

Key dates this semester (confirm against the course timeline if near a
boundary): the freeze starts at the **v1.5 deadline, Thu 23:59 of Week 11
(29 Oct 2026)**; final v1.6 submission **Tue 3 Nov 2026, 14:00**; PE starts
Fri 6 Nov 2026, 12:00 noon.

**Agent protocol — before editing anything under `src/main`, check today's
date.** If the freeze has started:

1. Warn the user that the feature freeze is active *before* making the
   change, and confirm the change is a necessary bug fix chosen by priority
   and regression risk. New features and enhancements are not allowed at
   all; known-but-unfixable flaws go into the DG "Planned Enhancements"
   appendix instead (section 7).
2. Keep the diff surgical. The freeze budget counts **every changed line
   under `src/main`, including comments, blank lines, and formatting** — so
   no refactoring, no reformatting, no renames, no drive-by cleanups.
3. Prefer exempt work where it achieves the goal: test code (`src/test`),
   documentation (including typo fixes), and non-code files do not count
   against the budget.
4. Remind the user to check their remaining budget on the course
   "tP Code Dashboard (Feature Freeze Period Only)".

Budget: each member may change at most **15% of the functional LoC
attributed to them, or 100 LoC, whichever is higher**, during the freeze.
There is a 5% grace band (penalties start past ~20%), and the penalty is at
least -2 marks, decided case-by-case per member.

**After the final v1.6 submission**, the published UG/DG pages and the repo
are required to match the submitted JAR. Do not push anything that changes
the published site or the product until the PE phases are fully over; any
planned repo cleanup (e.g., untracking this file — section 12) must be done
*before* the submission deadline.

## 10. Marks graded outside the PE (agents can still lose these)

PE bug hunting is only part of the tP grade. The following are assessed by
tutors and automated scripts from the repo and GitHub data — mistakes here
cost marks without any bug report ever being filed.

### 10.1 Code quality (manual inspection + scripts)

* Show evidence of all four: logging (`LogsCenter`), exceptions on error
  paths, assertions for internal invariants, defensive coding.
* Zero coding-standard violations. Checkstyle catches only a subset —
  naming (booleans sound like booleans), SLAP, and duplication are checked
  by humans. Per-PR specifics: sections 4 and 11.

### 10.2 Project management / process (tutor + script observed)

* Deliver iteratively and incrementally: small, frequent, working
  increments — never one big burst of work. Keep buffers; aim to finish at
  least 75% of a milestone's issues by its deadline.
* GitHub mechanics tutors look for: each task as a well-defined issue,
  assigned to a member, tracked in the correct `v1.X` milestone; milestones
  wrapped up on time; a GitHub release per version; feature-branch PRs with
  genuine reviews (avoid direct pushes to `master` for non-trivial work).
* Commit messages follow the course Git conventions
  (<https://se-education.org/guides/conventions/git.html>): imperative
  mood, capitalized subject, no trailing period, body explains what/why.
* Each member needs commits in at least 4 of weeks 7-12. Do not let one
  member's work accumulate into a single end-of-iteration mega-commit.

### 10.3 Authorship & attribution (RepoSense dashboards)

* Individual effort, code, and docs grading is cross-validated against Git
  data on the tP Code Dashboard. Always commit with the git identity
  (`user.name`/`user.email`) tied to the member's own GitHub account.
* Never commit one member's work under another member's identity —
  attribution follows the commit author, and disputes are settled against
  the dashboard.
* Markdown counts toward docs authorship; don't dump large generated text
  into the repo under one person's name without reason.

### 10.4 Automated tests (QA component)

* Test code itself is graded (alongside PE functionality bugs found in your
  features). Every feature you own needs meaningful automated tests; every
  bug fix needs a regression test (section 11).

### 10.5 Product website (tutor-checked; not PE-reportable)

* `README.md` / site home page: must describe the current product — no AB3
  leftovers — with working badges and links.
* `docs/images/Ui.png` must match the **current** GUI. Screenshot rules:
  realistic, well-populated data (no `test 123` values), clean crop of just
  the app window, decent resolution, no annotations/arrows/callouts.
* `docs/AboutUs.md`: each member with a recognizable recent photo (or the
  course-sanctioned placeholder for privacy), name or GitHub username,
  correct roles/responsibilities, and PPP links if PPPs are used.
* The published UG and DG pages must match the submitted v1.6 JAR.

### 10.6 Project Portfolio Page (PPP)

* Optional this semester, but it is the evidence used if contribution
  disputes arise. If written: `docs/team/<github_username_lowercase>.md`,
  following the AB3 example, and every claim must be consistent with the
  dashboards.

## 11. Mandatory verification before every commit/PR

Run and pass all of:

```sh
./gradlew check coverage   # checkstyle + tests + coverage (same as CI; Windows: gradlew.bat)
.github/run-checks.sh      # repo-wide text checks (run on macOS/Linux)
```

Repo hygiene enforced by CI (`.github/check-*.sh`) — applies to *every*
committed text file including Markdown:

* Every file ends with exactly one newline at EOF.
* LF line endings only (no CRLF).
* No trailing whitespace (hard error outside `.md`; keep `.md` clean too).

Additional expectations:

* Follow the course Java coding standard (checkstyle catches only part of
  it): boolean names read as booleans, SLAP (no long/deeply nested methods),
  no noticeable code duplication (including in tests), Javadoc for
  public/non-trivial members.
* Write/extend tests for every bug fix (regression test first, then fix) and
  every behavior change. Match existing test conventions in `src/test`.
* Keep PRs small and incremental (course requires breadth-first incremental
  delivery); do not batch unrelated changes.
* Do not edit generated artifacts (`build/`, `docs/_site/`, images generated
  from `.puml`) by hand; change the source instead.
* When UG/DG rendering matters, preview with MarkBind (the `docs.yml`
  workflow builds `docs/` on push to master) — broken MarkBind syntax renders
  literally and becomes a reportable formatting bug.
* If you used AI assistance or adapted external code for a non-trivial chunk,
  add the required credit (code comment at the reuse site, and DG/README
  acknowledgements as applicable) in the same PR.

## 12. About this file

* The course upstream `.gitignore` ignores agent files (`AGENTS.md`,
  `CLAUDE.md`, `/.claude/`, `/.codex/`). This team has deliberately removed
  only the `AGENTS.md` entry during development so the file can be
  version-controlled and improved collaboratively. Keep the other entries
  intact, and never place agent instructions inside `docs/` (everything
  there is published to the product website).
* **Cleanup before the final v1.6 submission** (tracked as a team issue,
  milestone v1.6 — do NOT leave this until after the deadline):

  ```sh
  git rm --cached AGENTS.md    # untracks it; local copies survive
  # restore the "AGENTS.md" line in .gitignore
  git commit -m "Untrack AGENTS.md for final release"
  ```

  Anyone cloning fresh afterwards can restore a local copy with
  `git show <last-tracked-sha>:AGENTS.md > AGENTS.md`.
* This file is not a course deliverable and is outside PE bug-reporting
  scope (PE covers only the product JAR and the published UG/DG pages).
* Course policy explicitly allows using AI tools for project work; any
  AI-generated content that ends up in graded deliverables must be
  acknowledged per the course reuse policy (see section 7,
  Acknowledgements).
