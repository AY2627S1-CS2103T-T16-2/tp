# AGENTS.md — TuitionBook

## Non-negotiables

* Preserve uncommitted work; never overwrite another contributor's changes.
* Do not commit, push, publish issues/reviews/releases, or deploy without permission.
* Use synthetic test data and disposable storage; protect real contacts and secrets.
* Keep released JAR behaviour and published UG/DG claims consistent; label future work.
* Run proportionate checks and disclose failures, skipped checks and uncertainty.

Keep critical prohibitions and approval requirements directly in this file,
not solely in linked references.

Team: `CS2103T-T16-2`; organisation: `AY2627S1-CS2103T-T16-2`; repository: `tp`.
TuitionBook is a contact-management application for private one-to-one home tutors.

**Project-policy precedence:** mandatory course rules > approved team decisions
and authoritative project documentation > this file's defaults. Higher-priority
agent safety/permission instructions still apply. Surface conflicting sources
with the user; do not invent prohibitions, approvals, evidence or grade guarantees.
**Course sources checked: 2026-10-04.** Recheck live rules when the task depends on them.

## Mandatory course requirements

These summarise explicit requirements in the [overview][overview] and
[constraints][constraints], not optional design advice:

* Evolve the supplied codebase incrementally, primarily using OOP, with contacts
  as the product's primary focus. The Morph direction is unavailable this semester.
* Target users who type fast and prefer typing; typed commands are the primary input.
* Keep the product single-user: no multi-user operation or regular sharing of a
  user's application data with other users.
* Store data locally in a human-editable text file, with manual-editing support
  at least at AB3's level. **Do not use a DBMS.**
* **Do not depend on a team-owned remote server.**
* Run on Windows, Linux and macOS with only Java 25 installed, without requiring
  an application installer or separate installation of application dependencies.
* Third-party libraries/frameworks/services need prior teaching-team approval
  (existing class-wide approval counts), free use and permissive licensing.
  Libraries/frameworks must be open-source; services are exempt from that condition.
* The GUI must work well at 1920×1080 and higher at 100%/125% scale, and remain
  usable at 1280×720 and higher at 150% scale.
* Deliver one executable JAR; if additional files cannot be packaged in it, use
  one ZIP containing the JAR and required files. The JAR/ZIP must not exceed 100 MB.
* Preserve the grading-sensitive `src/main/java`, `src/test/java` and `docs` paths.
  Keep main UG/DG content in their existing files, as required by [GitHub guidance][github]
  and the [deliverables instructions][deliverables].

## Approved team scope and contracts

* Read [DG Product scope](docs/DeveloperGuide.md#product-scope), the relevant
  [UG](docs/UserGuide.md) sections and the approved issue/spec before feature work.
  Product-specific exclusions are team decisions, not universal course prohibitions.
* Scope, command, validation, dependency and storage-format changes need approval;
  an explicit request approving that change provides the task's authority.
* Requirements describe intended needs, not proof of implementation. Keep priority,
  MVP scope and release status distinct. Do not implement unrequested future work.
* Honour approved model invariants and NFRs; verify current code and tests rather
  than relying on old conversations or treating every document as already implemented.

## Design recommendations — not additional prohibitions

* Prefer efficient CLI workflows, low interaction effort, minimal networking with
  suitable fallbacks, and easy-to-test features. These are design recommendations
  from the course; specific approved product guarantees remain binding.
* Avoid needless rejection of realistic input. Consider warnings only where accepting
  the value is compatible with the agreed contract and data integrity. Clearly reject
  inputs that cannot satisfy those requirements. Propose validation changes rather
  than silently relaxing validators or adding new restrictions.
* Prefer simple, cohesive improvements and existing abstractions over speculative
  complexity. Explain trade-offs; do not treat personal preferences as grading rules.

## Working procedure

1. Inspect branch, working changes and applicable instructions. Confirm the human
   owner, issue/milestone, acceptance criteria, non-goals and affected components.
2. Before code changes, check the active iteration and any freeze restrictions using
   the sources below. Stop for clarification if permission or remaining budget is unclear.
3. Trace parser → command → model → storage/UI and read related tests. Resolve
   contradictions between approved intent, documentation, tests and implementation
   before choosing a fix; never edit docs merely to legitimise a defect.
4. Reproduce bugs and add a failing regression test where feasible, then fix the root
   cause. Derive feature tests from acceptance criteria, not implementation assumptions.
   Add a short instruction only when it helps prevent a recurring project-specific
   mistake; capture individual bugs in regression tests where feasible.
5. Make a small, buildable change. As a team workflow default, include affected tests,
   UG/DG sections, diagrams and help/messages in the same PR as behaviour changes.
   Avoid unrelated upgrades, formatting or renaming.
6. Review the final diff, run relevant checks and report results. Do not disable tests
   to hide failures, weaken security/CI policies, fabricate evidence or hide incomplete work.

When multiple agents are requested, assign disjoint write areas and agreed interfaces;
serialise shared-contract edits and retest after integration. Agent reviews do not
replace genuine teammate review. The responsible human must understand the work.
Do not change Git identity, rewrite history or delete PR branches automatically;
consult the course workflow before authorised Git operations.

## Code and test essentials

* Follow the [Java standard][java] and existing component boundaries. Avoid duplicated
  logic, oversized methods and catch-and-ignore handling. Use project logging and
  exceptions appropriately; assertions must not be the only input validation.
* Handle plausible input mistakes without crashing or corrupting data; give useful
  error messages and recovery guidance rather than misleading success messages.
* Protect identity, relationships and persisted data. Validate at the appropriate
  domain boundaries; rejected operations must not leave unintended partial changes.
  Distinguish model atomicity, successful persistence and recovery after write failure.
* Agree compatibility/migration before schema changes. Do not silently discard valid
  records or promise stronger backup/recovery guarantees than are implemented.
* Use deterministic, isolated tests following existing JUnit conventions. Cover
  relevant valid, invalid and boundary inputs, messages, filtered/stateful workflows,
  relationship integrity, JSON round-trips and failure recovery. Test GUI/platform
  behaviour when affected; coverage alone is not proof of correctness.
* Check changed documentation examples and rendered output. Keep future requirements
  explicit; do not invent benchmark results or weaken NFRs to conceal defects.

## Proportionate verification

Resolve tasks/scripts from current `build.gradle`, CI workflows and `docs/package.json`;
these commands are defaults, not mandates to run obsolete tasks. Do not silently omit
an equivalent check after a rename.

* **Java/functional changes:** verify Java/Gradle versions, run affected tests, then
  configured CI-equivalent checks, normally `./gradlew check coverage`. Use the
  appropriate Windows wrapper where needed; a local pass is not a CI matrix pass.
* **Packaging/resources/releases:** run the configured JAR task, normally
  `./gradlew shadowJar`. Resolve its output from build configuration and smoke-test
  the actual artifact with Java 25 in disposable writable storage, including restart.
* **Published docs:** use the locked dependencies and configured scripts, normally
  `npm --prefix docs ci` when needed, then `npm --prefix docs run build` and local
  preview. Inspect rendering and examples; do not deploy merely to preview.
* **Text/instructions:** normally `.github/run-checks.sh` and `git diff --check`.
  Some checks inspect the Git index, so also review unstaged/untracked changes.
  `AGENTS.md`-only changes do not require an unrelated Java rebuild.

Investigate failed checks, distinguishing regressions from environment/existing issues.
Ask for help with authentication, permissions or configuration decisions. Never claim
an OS, UI, artifact, example or NFR was verified unless actually checked.

## Read on demand — before the corresponding task

Keep detailed procedures in their authoritative sources instead of duplicating them here.
If a required source cannot be accessed, disclose which source is unavailable and
seek clarification before proceeding with work that depends on it. Do not guess.

| Task | Required references |
|---|---|
| Feature/design/dependency changes | Relevant approved issue/spec, UG/DG, [constraints][constraints] and [expectations][expectations]. |
| Tests or alpha testing | [Testing guide](docs/Testing.md), neighbouring tests and [course alpha-testing guidance][alpha]. |
| UG/DG/diagram changes | [Documentation guide](docs/Documentation.md), [deliverables][deliverables], [grading][grading] and [requirements notation][notation]. |
| Iteration planning or Git/PR actions | [Timeline][timeline], its active week's instructions and [GitHub workflow][github]. |
| Code/release changes near deadlines or after submission | [Timeline][timeline] and [freeze/submission rules][release]; verify current limits, permissions and required artifacts before acting. |
| PE reports/responses/evaluations | [PE rules][pe] for the active phase and [grading][grading]; verify facts, impact and allowed issue edits, not presumed severity. |
| AI assistance or external reuse | [AI guidance][ai] and [reuse/attribution policy][reuse]; credit actual tools, authors and extent, preserving human ownership. |

## Handoff

Report **Changed**, **Verified** (exact commands/scenarios, observed results and
supporting output or artifact references), **Not verified** (with reasons), and
**Risks/decisions** (including approvals still needed). Merely stating that the
rules were followed is not verification evidence.
Use the response or existing issue/PR; do not create extra reports or instruction
files unless requested. Keep shared guidance tool-neutral, outside published docs,
and limited to stable instructions rather than dated feature-status claims.

[overview]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-overview.html
[constraints]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html
[expectations]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-expectations.html
[deliverables]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-deliverables.html
[grading]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html
[github]: https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixE-gitHub.html
[timeline]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-timeline.html
[alpha]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w11.html
[release]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w12.html
[pe]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-pe.html
[ai]: https://nus-cs2103-ay2627-s1.github.io/website/admin/courseExpectations.html#use-of-ai
[reuse]: https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixB-policies.html#policy-reuse
[notation]: https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/specifyingRequirements.html
[java]: https://se-education.org/guides/conventions/java/intermediate.html
