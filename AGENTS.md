# AGENTS.md — Working on TuitionBook (CS2103T tP)

Team ID: `CS2103T-T16-2`; GitHub organisation: `AY2627S1-CS2103T-T16-2`;
repository: `tp`. TuitionBook evolves AddressBook-Level3 (AB3).

**Goal:** a useful, cohesive, maintainable product with reproducible evidence of
correctness, not maximum feature count or a promise of "full marks". Grades also
assess individual contributions, code quality, documentation, testing, reviews,
and process. Passing tests does not prove absence of bugs. The course expects
some bugs; not every accepted report causes a deduction. Do not hide defects or
optimise for rejecting reports.

**Course sources checked: 2026-10-04.** Recheck the live sources and announcements
before deadline-sensitive work. Distinguish course rules, recommendations, and
our stricter team policies. Resolve uncertainty with the user; do not invent
rules or grades. Higher-priority agent safety/permission instructions still apply.

## 1. Task execution gate

1. **Inspect the actual state:** branch, `git status`, existing changes and nested
   instructions. Preserve others' work. Old chats, IDE tabs and PR descriptions
   are not evidence of the current implementation.
2. **Confirm scope:** human owner, iteration, user need, acceptance criteria,
   affected files and non-goals. Identify the issue/milestone for non-trivial
   work. Ask before expanding scope, changing command contracts, adding
   dependencies, or changing persisted data. Check section 11 for freeze limits.
3. **Read before editing:** trace parser → command → model → storage/UI, inspect
   neighbouring tests and affected UG/DG sections, and reuse existing utilities.
4. **Resolve the intended contract:** compare the approved task/specification,
   UG/DG, code and tests. Report disagreements. Do not silently rewrite docs to
   legitimise a bug or implement every future story simply because it is listed.
5. **Reproduce bugs first:** record starting state, minimal input, expected/actual
   results and environment. Add a regression test that fails for the right reason,
   then fix the root cause. Derive feature tests from acceptance criteria, not
   by copying the implementation's assumptions.
6. **Make a small coherent change:** include affected tests, messages, fixtures
   and documentation. Avoid unrelated renaming, formatting, upgrades or rewrites.
   Keep intermediate work buildable; review the full final diff.
7. **Verify and hand off:** run appropriate checks from section 8. Disclose
   failures, blocked checks, skipped checks and remaining risks. Never claim an
   untested GUI, OS, JAR, website or NFR was verified. Do not quietly drop work.

### Multiple agents

* Delegate only when the user requests agents/subagents. Give each task a human
  owner, base revision, precise scope, shared behaviour/interface contract,
  expected tests and a disjoint write area. Use separate branches/worktrees for
  authorised concurrent writers; never switch another writer's branch.
* Serialise edits to shared parsers, model interfaces, schemas, build files and
  UG/DG sections. Agree roles, IDs, filters and errors before parallel coding.
* Require handoffs with changed files, contract changes, tests run and unresolved
  risks. Re-read and retest the integrated result after conflict resolution;
  two passing branches do not prove that their combination works.
* Review from the user contract, not just the implementer's explanation. An
  independent agent review can help but does not replace the expected teammate
  review. The human owner must understand the generated code and trade-offs.

## 2. Product direction and scope

The target user is a private one-to-one tutor who travels to students' homes,
types quickly and prefers typing. Contacts and student–guardian relationships
remain central. Follow the current DG product scope and approved team decisions;
do not independently add scheduling, billing, academic tracking, messaging,
multi-user access or sync.

* Prefer complete, easy-to-use tutor workflows over many shallow features.
  Evaluate total interaction effort, including repeated lookups, filters and
  mouse actions, rather than command execution time alone.
* The course's 500–600 functional LoC per person is a **ballpark**, not a quota
  or guarantee of marks. There is no extra effort credit for exceeding the bar.
  Do not pad code, manufacture features or fabricate activity for credit.
* Priority (`***`, `**`, `*`), MVP scope and implementation status are different.
  Retain identified future requirements, clearly labelled, without advertising
  them as available functionality.
* Before coding, settle accepted values, optional/default fields, prefix order
  and repetition, unknown input, normalisation, duplicates, outputs, failures,
  filter effects and persistence. Record decisions in the existing issue/spec
  or relevant docs, not an unrequested new planning file.
* Validation changes are product decisions. Course examples about permissive
  phones, aliases, warnings and search do not authorise silently changing our
  grammar, duplicate key or filter semantics. Explain trade-offs and obtain
  agreement before updating code, tests and documentation together.

## 3. Course constraints and team policies

Read [the current constraints][constraints] before product, dependency or
packaging changes. Do not turn recommendations into invented hard prohibitions.

* **Brownfield, incremental, primarily OO:** evolve AB3, keeping contacts central.
  The Morph direction is unavailable this semester. Keep the existing Java
  architecture unless an approved change has a concrete benefit.
* **Typing-preferred:** typed commands are primary. CLI-first efficiency is a
  recommendation/product-design criterion, not a ban on GUI interactions.
  Honour TuitionBook's documented keyboard guarantees and provide CLI
  alternatives where appropriate.
* **Single user/local:** no shared-user/shared-data workflows. Normal contact
  management must work offline under our scope/NFRs. The course prohibits
  dependence on our own remote server, not all networking; reliable public APIs
  are permitted subject to other rules. Networking/fallbacks need careful
  testability decisions and team approval.
* **Human-editable local text storage; no DBMS:** support manual edits at least
  as well as AB3. Correctly edited files must work. Any stronger backup/recovery
  promise in our UG/DG must be implemented and tested; AB3's warning about data
  loss after incorrect edits does not cancel our stronger promises.
* **Java 25/portable:** run on a computer with only Java 25, without an application
  installer or separately installed JavaFX/libraries. Do not change the Java,
  JavaFX, Gradle or packaging configuration as an unrelated fix.
* **Windows/Linux/macOS:** check path separators, case-sensitive filenames,
  encoding, locale-sensitive comparisons, resources and native libraries. Do not
  promise all future Java versions or untested environments via "or later".
* **Dependencies:** verify course approval and licences; prior class-wide
  approval may be reused. Libraries/frameworks must be free, open-source and
  permissively licensed, with no user installation. Services need not be
  open-source; account requirements are strongly discouraged. Prefer existing
  dependencies and vetted, bounded versions. Never weaken security/CI controls
  or dependency policies to obtain a passing build.
* **Display:** work well at 1920×1080 and higher at 100%/125% scale; remain usable
  at 1280×720 and higher at 150% scale. Essential information/actions must remain
  reachable when resizing, scrolling and displaying long content.
* **Distribution:** prefer one executable JAR; one ZIP is permitted if additional
  files cannot be packaged in the JAR. The JAR/ZIP limit is 100 MB. Avoid heavy
  unnecessary assets/dependencies. `gradlew run` does not verify the release JAR.

## 4. Implementation and data integrity

* Preserve parser/command/model/storage/UI boundaries. Essential domain
  invariants must not exist only in a parser or JavaFX controller, where other
  callers or deserialisation could bypass them.
* Follow the [Java standard][java], SLAP and existing patterns. Boolean names
  should read as booleans. Avoid duplicated logic/tests, deep nesting, broad
  catch-and-ignore blocks and speculative abstractions. Checkstyle is not a
  substitute for reviewing naming, method size, cohesion and maintainability.
* Use exceptions for expected error paths, assertions for internal invariants,
  and `LogsCenter`/`java.util.logging` appropriately. Assertions may be disabled
  in the JAR: never use them as the only input validation. Do not sprinkle
  assertions/logging solely to tick a grading box.
* Keep identity, equality, hashing, duplicate detection and normalisation
  consistent. Field changes may affect builders, fixtures, sample data,
  predicates, JSON adapters, UI bindings and tests; trace these dependencies.
* Preserve stable contact IDs and referential integrity. In the one-guardian
  MVP, one guardian may serve many students. Replacement/repeated linking and
  deletion must preserve the agreed invariants. Deleting a guardian must not
  cascade-delete students. Never identify relationships by mutable names or
  transient displayed indexes.
* Validation rejection must not partially mutate contacts, links, filters or
  persisted data unless explicitly specified. Distinguish storage errors:
  model atomicity is not disk atomicity, and failed writing is not saved success.
* Agree schema compatibility/migration before changing storage. Consider old
  files, absent optional fields, malformed required values, duplicate IDs,
  hand-edited duplicates and invalid links. Do not silently discard valid records
  or regenerate established IDs to make loading succeed.
* Derive file locations from code: a relative path may resolve against the
  working directory, not the JAR directory. Verify before documenting either.
* Use synthetic data and temporary directories. Never test against or overwrite
  real contacts. Keep personal data, gate codes, credentials and sensitive raw
  commands out of commits, external tools, evidence and unnecessary logs.

## 5. Risk-based test matrix

Trace **requirement → implementation → tests → documentation** for changed
behaviour. Select relevant cases below; not every PR needs the entire matrix.

### Input and messages

* Typical/minimal/maximal valid inputs; absent optional fields; empty and
  whitespace-only values; leading/trailing/repeated spaces; supported Unicode.
* Missing mandatory prefixes, repeated single-valued prefixes, repeatable tags,
  unknown/out-of-order prefixes, unexpected preambles/trailing text, missing
  separators, pasted multi-line text and prefix-like text inside field values.
* Indexes: zero, negative, non-numeric, overflow and out of displayed range.
  Distinguish malformed indexes from valid integers outside the current list.
* Realistic names, addresses and phones: apostrophes, hyphens, accents, `s/o`,
  `d/o`, leading `+`, spaces and punctuation; test length boundaries and long
  display values. Neither impose arbitrary restrictions nor discard agreed
  validation just to accept all strings.
* Case/whitespace/phone normalisation; same name with different phones, same
  phone with different names, and edits that duplicate another contact.
  Preserve documented distinctions; do not invent country-code equivalence.
* Check exact useful messages as well as exceptions/status. Identify the actual
  field/problem and recovery action, without confusing invalid format, invalid
  value, duplicate, wrong role and out-of-range index.
* Cross-check extra-argument behaviour against both generic and command-specific
  UG rules, especially role-filtered `list`. Inherited AB3 behaviour is not
  automatically correct or consistent with updated docs.

### State, persistence and integration

* Exercise add → find/filter → edit/link/view/delete → list → restart sequences.
  Assert records, relationships, displayed list and persisted state, not just
  success messages or mocked calls.
* Cover empty/no-match/many-match lists and reindexing after mutations. Respect
  agreed filter replacement/combination semantics. Both indexes in a link
  command, and all other index-based commands, refer to the displayed list.
* Cover linked/unlinked students/guardians, shared guardians, renaming, link
  replacement/repetition and deletion in both directions. Honour idempotence
  where promised; no dangling references or unintended deletion of other contacts.
* Round-trip through actual JSON adapters/storage and reload a fresh model.
  Verify IDs, roles, optional fields, tags, links and new attributes. Separately
  test valid manual edits, missing files, malformed files and invalid links,
  including promised warnings/backups and overwrite timing.
* Inject save/load failures using test doubles or disposable files. Verify
  in-memory state, reported failure, existing-file preservation where promised,
  and the outcome after restart. Never use real data to test destructive paths.
* Use existing JUnit 5 conventions and deterministic, isolated tests. Avoid test
  order dependencies, real networking, sleeps, machine locale and developer data
  directories. Assess related commands/shared causes when fixing a defect.
* Never disable/delete failing tests or change expected results without evidence
  that the intended contract changed or the test was wrong. Coverage locates
  gaps; it does not prove correctness. The course has no minimum percentage.

### UI and user workflow

* Verify cards, results, counts, filters and relationship displays update after
  mutation. Respect JavaFX threading and avoid blocking the UI unnecessarily.
* Check required resolutions/scales, long content, keyboard focus, scrolling,
  resizing, saved geometry, off-screen recovery and secondary/help windows.
  Exiting must not leave application windows/processes behind.
* Test a tutor's complete workflow, not just isolated commands. Can they reach
  the correct guardian/address with low typing and navigation effort? Include
  legitimate unusual input; hostile-input tests do not replace usability tests.

## 6. Documentation contracts

Update **affected** UG/DG sections, examples, help/messages, diagrams and manual
checks with behaviour changes; do not churn unrelated sections. Descriptions of
current behaviour must match the released JAR. Clearly labelled future
requirements/proposed implementations may legitimately differ.

### User Guide

* Keep main content in `docs/UserGuide.md`. Clearly state target user/value,
  prerequisites, launch command and initial-data behaviour. Cover current
  features, useful examples, validation, interactions and limitations; label
  future features "Coming soon". Rare details need not be repeated when users
  already know them or the application explains them clearly.
* Literally execute changed examples with their prerequisites. Check summaries,
  generic format notes, detailed sections, screenshots and help against each
  other. Do not introduce broad claims that contradict a command-specific rule.
* Inspect rendered **HTML**, not only Markdown: headings, anchors, tables,
  images, links, readability and copy-paste commands. If distributing a PDF,
  additionally check pagination/copying; do not invent a PDF submission duty.
* Known issues are not immunity. Explain impact/workarounds honestly; do not
  disguise broken required behaviour as a restriction on the user.

### Developer Guide

* Keep main content in `docs/DeveloperGuide.md`; do not split UG/DG into pages
  contribution scripts will miss. Explain design, relevant alternatives and
  trade-offs for a future developer. Do not present unimplemented designs as fact.
* **Stories:** actor/capability/benefit should match, with important needs covered.
  Separate priority, MVP scope and status; include inherited and retained future
  needs. Do not manufacture stories for a count or promise every story will ship.
* **Use cases:** document representative non-trivial interaction patterns, not
  automatically one per command. Keep MSS self-contained and user-observable.
  Check preconditions, extension conditions/numbering, end/resume points and
  guarantees; do not put internal verification steps in the success flow or
  claim a branch is impossible without testing that logic.
* **NFRs:** separate functional actions from operational constraints/qualities.
  Specify verifiable workload/environment and success criteria. Preserve fault
  boundaries (e.g., normal restart versus write failure), rather than promising
  no data loss under all circumstances. Verify current NFRs before release;
  do not invent benchmarks or weaken requirements to conceal defects.
* **Glossary:** define noteworthy domain terms precisely, including recorded
  links versus real-world responsibility and exact normalisation. Scope MVP-only
  restrictions and avoid unnecessary terms.
* **UML:** follow course notation; maintain `.puml` sources in `docs/diagrams/`
  and inspect rendering. Check relationships, relevant multiplicities, arrows,
  lifetimes and consistency with code. Optional omissions are fine if not
  misleading; inconsistent omissions can still harm readability.
* Use appropriate diagram types, not gratuitous complexity. Keep architecture
  high-level, normally show one component's internals per sequence diagram,
  explain diagrams nearby and keep text legible. Avoid excessive code listings.
* **Manual testing appendix:** cover user-testable features (unchanged AB3
  features are exempt), with reproducible setup, inputs and expected results.
  Complement the UG; cover important relationship/persistence failures and
  remove unfinished instructions before release. Do not claim unexecuted checks.
* Credit reuse as in section 13. Retain clearly labelled proposed designs if
  useful, but remove irrelevant placeholders and inaccurate template claims.

### Planned Enhancements

Only after v1.5, the optional DG `Planned Enhancements` appendix may contain at
most `team_size × 2` numbered items, stating team size at the start. Each item
must be **one specific tweak to an existing feature**, explaining the flaw and
precise intended behaviour, with examples where useful.

Protection applies to matching `type.FeatureFlaw` reports, not every bug in the
feature. Testers may challenge inadequate/flawed proposals or bundled items.
Excess entries are unprotected and exceeding the limit is reportable. Do not use
broad entries such as "fix validation", new features, or an early placeholder
appendix to avoid necessary fixes. Read the current [deliverables][deliverables]
and [PE rules][pe].

## 7. Quality and scope of review findings

* Distinguish confirmed defects, plausible risks and optional suggestions. Cite
  current evidence and user impact; preferred wording, missing simple use cases,
  or style differences are not automatically bugs.
* An implementation matching the UG can still be a poor design. Conversely,
  finding a mismatch does not automatically tell you whether code or docs should
  change. Resolve intended behaviour rather than choosing the easiest edit.
* Review failure paths, lost updates, invalid state, persistence, compatibility
  and realistic input independently of the author's explanation. A green CI
  badge, high coverage or another agent saying "LGTM" is not sufficient review.
* Human teammates should provide genuine, specific, location-based PR reviews.
  The human owner must be able to explain generated code, tests and trade-offs.

## 8. Verification commands and evidence

Use `build.gradle`, `.github/workflows/gradle.yml`, `.github/run-checks.sh`,
`docs/package.json` and `.github/workflows/docs.yml` as the toolchain references.
For Java/functional changes, run affected tests during iteration, then:

```sh
java -version
./gradlew --version
./gradlew check coverage
.github/run-checks.sh
git diff --check
```

* In native Windows shells, use `gradlew.bat`. Inspect actual Linux/macOS/Windows
  CI results where available; a local pass does not establish a matrix pass.
  `coverage` produces reports, not a guarantee of adequate tests.
* Text scripts use the Git index for some checks: they do not establish that all
  unstaged/untracked content was checked. Inspect the working diff/new files and
  rerun on intended staged contents when committing is authorised. Use LF, a
  final newline and no trailing whitespace. Do not stage unrelated work.
* For published-doc changes: `npm --prefix docs ci` if installation is needed,
  then `npm --prefix docs run build`; inspect rendered pages. Preview with
  `npm --prefix docs run serve`, not deployment. Compare local/CI MarkBind
  versions and report mismatches rather than silently upgrading dependencies.
* For packaging/resources/dependencies and releases: `./gradlew shadowJar`.
  The configured output is `build/libs/addressbook.jar`. Launch that JAR using
  Java 25 in a disposable writable folder, without relying on IDE settings or
  enabled assertions; test startup, resources, shutdown and persistence.
* `AGENTS.md`-only changes need content/source review and text/diff checks, not
  an unrelated Java rebuild. Choose checks proportionate to changes and state
  omissions. UG-only changes still need relevant rendering/example checks.
* Investigate failures and distinguish regressions from pre-existing/environment
  problems with evidence. Report exact failed/blocked commands. Never disable
  checks, relax tests or weaken security policies for a green result. Ask for
  help with authentication, permissions or configuration decisions.

## 9. Iteration, ownership and workflow

* Deliver breadth-first, incremental improvements, not an AI-generated semester
  in one burst. Keep early coding understandable; the course discourages moving
  more than one week ahead. Prefer feature ownership spanning code, tests and
  UG/DG, with component owners guiding/reviewing shared changes.
* Use meaningful issues, human assignees, `v1.X` milestones, focused PRs and
  buffers. Assign both issues and PRs to milestones; move unfinished work forward
  honestly. Never fabricate completed tasks, reviews, tests or contributions.
* Through v1.3, use separate branches of individual forks, not `master`. After
  v1.3 the team may agree to use branches in the team repo. Preserve PR branches;
  prefer merge commits over rebase/squash that can alter contribution timestamps.
* No automatic commits, pushes, public issue/review posts, merges, releases,
  history rewriting, branch deletion or Git identity changes. Obtain the user's
  authorisation for publishing actions. Before an authorised commit, verify the
  existing author identity belongs to the responsible human; ask if incorrect.
* Follow [Git conventions][git]: imperative, capitalised subject, no trailing
  period; explain why where useful. Settle honest RepoSense attribution before
  freeze, including AI-assisted work and reused code.
* Track genuine individual code, tests, UG/DG, reviews and team-task work. Course
  expectations include commits in four of weeks 7–12, parallel PRs at least once
  per member, and at least 75% of project tasks completed on time; do not game
  these signals. A second agent is not a second student contributor.

| Stage | Gate; consult the linked current instructions |
|---|---|
| [W3][w3] / [W4][w4] | Team meetings, public-view project notes, target user/value; no invented persona facts. |
| [W5][w5] / [W6][w6] | Gather/prioritise requirements, choose a small MVP, agree detailed feature contracts. |
| [W7 / v1.1][w7] | Practise fork/branch/PR/review workflow; update website, direction and DG requirements. |
| [W8 / v1.2][w8] | Small functional increments without breaking code/tests; no packaged release required yet. |
| [W9 / v1.3][w9] | Working MVP JAR, release notes/screenshots, on-time milestone closure. |
| [W10 / v1.4][w10] | Postmortem, rough versions of intended final features, release, initial UML and parallel PRs. |
| [W11 / v1.5][w11] | Alpha-test the JAR, resolve important defects, improve code/tests/docs, settle authorship, release. |
| [W12 / v1.6][w12] | Risk-controlled fixes, freeze compliance, verified submission, published UG/DG and PE. |
| [W13][w13] | PE responses/evaluations; no unauthorised fixes to submitted code. |

Alpha testing suggests two non-author testers per feature. Each student needs
at least five meaningful `alpha-bug` issues for that individual task to count;
these may include genuine improvement suggestions under the alpha instructions.
Do not invent bugs to reach five or confuse this with PE's defect-report rules.

## 10. Release and submission gate

1. Build the intended revision with the required Java. Record commit, checks,
   artifact identity/checksum and tested platforms. Rebuild/retest the final JAR
   after any source/resource change; verify the artifact actually uploaded.
2. Smoke-test the JAR in a clean writable directory with synthetic data: first
   launch, tutor workflow, invalid input, relationships, shutdown/restart and
   documented recovery. IDE execution or mocked tests are not substitutes.
3. Verify supported platforms and required display settings where available;
   request teammate/forum smoke tests for unavailable environments and disclose
   gaps. Check size/resources, a renamed JAR and paths with spaces. Do not depend
   on source-tree files, runtime downloads, extra installations or developer tools.
4. Inspect the final **published** UG/DG against the artifact: links, screenshots,
   examples, messages, summaries, manual tests and current NFR claims. Keep
   README/site badges/branding, `AboutUs` and `docs/images/Ui.png` accurate.
   Use realistic synthetic screenshot data, clean crops and no annotations.
   Internal AB3 package names need not be renamed for branding.
5. Follow the active version's release/submission rules. Final Canvas naming is
   `[CS2103T-T16-2][TuitionBook].jar`; the build artifact can retain its configured
   name. Verify team ID, product name, release tag, source revision and website.
6. Have another member check the submission with time to spare. Publishing,
   uploading and closing milestones require authorisation. Repeated identical
   late uploads are not harmless: the latest submission timestamp counts.
   Follow both courses' applicable submission instructions.

**As checked this semester:** the product demo deliverable is removed; PPP is
optional unless requested for a contribution dispute or chosen by the student.
Do not create unnecessary deliverables from obsolete templates.

## 11. Freeze and post-submission protection

Recheck [W12][w12], the calendar and announcements. As of 2026-10-04, feature
freeze begins **29 Oct 2026, 23:59** (v1.5 deadline); final submission is
**3 Nov 2026, 14:00**; PE Phase 1 is **6 Nov noon–7 Nov noon**.
Use the course's Singapore time context, not an assumed local timezone.

* The enforced per-member limit is **15% of functional LoC or 100 LoC, whichever
  is higher**. The dashboard uses attributed functional code at the end of v1.6,
  not a guessed local diff size. Confirm the remaining budget with the user.
* Code-file changes under `src/main` include comments, blank lines and formatting.
  Tests, documentation and non-code files such as images are exempt from the
  budget, not from correctness or other release restrictions.
* The course allows a 5-percentage-point margin; do not plan to consume it.
  Do not compress/reclassify code, manipulate attribution or borrow another
  member's quota to evade the limit.
* **Course rule versus team policy:** the course enforces the amount of code
  changed. New features are strongly discouraged, not categorically prohibited.
  Our default is necessary low-risk fixes only; anything else needs explicit
  user/team agreement, budget review and a regression plan. Keep diffs surgical.
* Planned Enhancements protection is limited as described in section 6; it does
  not justify an unusable product or inaccurate documentation.
* **No code updates for 30 days after the final deadline without teaching-team
  permission**, except approved late submissions. Completion of PE response
  phases does not end this rule. Preserve submitted artifacts and corresponding
  published UG/DG; do not republish a different product/site during evaluation.
* Permitted issue/milestone/PR metadata updates can continue. PE responses do not
  require code fixes. Do not merge, push, upload replacement artifacts or perform
  release cleanup without checking restrictions and required permissions.

## 12. PE scope and honest triage

Consult [the current PE rules][pe] for the active phase. PE targets the JAR and
published UG/DG HTML. Other website pages/README and code quality are outside PE
report scope, but may affect other assessment. Data/config and terminal output
can matter when they affect behaviour or unnecessarily alarm users.

* We own shipped defects, including inherited AB3 bugs. Prioritise data loss,
  crashes, wrong results, misleading contracts, usability and cosmetics using
  impact, realistic triggering conditions and regression risk.
* Types: `FunctionalityBug` = incorrect behaviour; `FeatureFlaw` = inadequate
  design/feature fit/constraint compliance; `DocumentationBug` = flawed docs.
  PE labels use the `type.` prefix. Do not pick labels to manipulate marks.
* `severity.VeryLow` is cosmetic; `Low` is rare minor inconvenience; `Medium` is
  occasional inconvenience while still usable; `High` means major problems for
  most users, making the product almost unusable. Consider reader impact for
  docs; not every crash or missing requirement automatically merits `High`.
* Typos are reportable, including UI typos. Minor grammar issues that do not
  hinder readers have different triage treatment. Known issues reduce impact,
  not automatically liability. `NotInScope` needs the course's priority and
  graceful-handling justification, not merely a label/disclaimer.
* Reproduce against the submitted version/state/platform. Prove duplicates by
  shared inseparable cause, not similar symptoms. Accept reasonable defects;
  do not fabricate an original design rationale after receiving a report.
* Make reports self-contained: starting data, exact steps, expected/actual result,
  environment, evidence and impact. PE needs one bug per issue and exactly one
  type/severity label. Do not copy others' discoveries as the user's own testing,
  fabricate screenshots, or automatically file speculative findings.
* AI/automated testing tools are allowed, but PE work is individual. The student
  must verify agent findings and make the required evaluations themselves.
* Read phase-specific editing permissions/templates/deadlines. Initial report
  evidence belongs in the issue body; developer responses must not edit the
  tester's original title/body. Do not transfer reports, change provided labels,
  post responses or submit evaluations without authorisation.

## 13. AI, reuse and shared instructions

Follow [AI guidance][ai] and [the reuse policy][reuse]. AI use is expected, but
the human remains responsible and must understand the work. An agent cannot
provide teaching-team approval or guarantee marks.

* Localised AI assistance: cite it in comments near the affected work. Widespread
  assistance: the policy permits DG Acknowledgements stating tool, human user
  and extent instead of per-site comments. Do not demand both universally.
* Cite external inspiration/adaptation where used. Copied non-trivial blocks
  with minor edits also need prescribed RepoSense `-reused` markers; consult the
  policy before applying them. Credit libraries in DG Acknowledgements.
* Documentation, diagrams and media also need appropriate attribution/licences.
  Ordinary following of AB3 patterns, intra-team reuse and course instructional
  materials have exceptions; do not invent extra requirements. Retain the
  project's AB3 acknowledgement. Never invent research, benchmarks or credits.
* `AGENTS.md` is shared guidance, not a PE deliverable. The course explicitly
  [encourages shared agent files][github]. **It does not require untracking this
  file before v1.6.** Do not delete/untrack it or edit `.gitignore` automatically;
  any team-specific decision needs confirmation.
* Keep instructions outside `docs/`. Keep this file actionable, not a copy of the
  website or a store of stale product facts. New Devin-specific configuration
  belongs in `.devin/`; do not create other tools' configuration unless asked.
* Do not rename `src/main/java`, `src/test/java` or `docs`, or split main UG/DG
  content into extra pages that grading scripts will miss.

## 14. Definition of done and handoff

Ready for review means acceptance criteria addressed, affected contracts/tests/
docs consistent, relevant checks actually run, and uncertainty disclosed.
Blocked or partial work must not be silently labelled complete. End with:

* **Changed:** what and why; relevant files/issue.
* **Verified:** exact commands/scenarios, results, environment and revision/JAR
  where relevant; distinguish new regression coverage from existing tests.
* **Not verified:** unavailable OS/UI tests, blocked tooling and skipped checks
  with reasons. Do not turn these into a claim of complete release readiness.
* **Risks/decisions:** spec conflicts, compatibility/migration concerns, known
  defects, freeze limits and outstanding approvals/human review.

Use the response or existing issue/PR for evidence; do not create extra report
or planning files unless requested.

## Course reference index

Use current sources rather than this summary for policy decisions:
[overview][overview], [expectations][expectations], [grading][grading],
[constraints][constraints], [deliverables][deliverables], [PE][pe],
[timeline][timeline], [team communication][teams], [supervision][supervision],
[GitHub workflow][github], [AI][ai], [reuse][reuse], [Git conventions][git],
[Java conventions][java], [requirements/use cases][specifying],
[NFRs and verifiability][requirements], and the weekly links in section 9.

[overview]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-overview.html
[expectations]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-expectations.html
[grading]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html
[constraints]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html
[deliverables]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-deliverables.html
[pe]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-pe.html
[timeline]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-timeline.html
[teams]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-teams.html
[supervision]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-supervision.html
[github]: https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixE-gitHub.html
[ai]: https://nus-cs2103-ay2627-s1.github.io/website/admin/courseExpectations.html#use-of-ai
[reuse]: https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixB-policies.html#policy-reuse
[git]: https://se-education.org/guides/conventions/git.html
[java]: https://se-education.org/guides/conventions/java/intermediate.html
[specifying]: https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/specifyingRequirements.html
[requirements]: https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/requirements.html
[w3]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w3.html
[w4]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w4.html
[w5]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w5.html
[w6]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w6.html
[w7]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w7.html
[w8]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w8.html
[w9]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w9.html
[w10]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w10.html
[w11]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w11.html
[w12]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w12.html
[w13]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w13.html
