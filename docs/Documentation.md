---
  layout: default.md
  title: "Documentation guide"
  pageNav: 3
---

# Documentation Guide

* We use [**MarkBind**](https://markbind.org/) to manage documentation.
* The `docs/` folder contains the source files for the documentation website.
* To learn how set it up and maintain the project website, follow the guide [[se-edu/guides] Working with Forked MarkBind sites](https://se-education.org/guides/tutorials/markbind-forked-sites.html) for project documentation.

**Style guidance:**

* Follow the [**_Google developer documentation style guide_**](https://developers.google.com/style).
* Also relevant is the [_se-edu/guides **Markdown coding standard**_](https://se-education.org/guides/conventions/markdown.html).


**Converting to PDF**

* See the guide [_se-edu/guides **Saving web documents as PDF files**_](https://se-education.org/guides/tutorials/savingPdf.html).

## Keeping the guides current

Update the guides in the same pull request as a command or visible behaviour change. Write the UG for tutors completing tasks and the DG for maintainers understanding the implementation.

Before each release:

1. Check command formats, examples, messages and recovery instructions against the release candidate. Run examples in disposable storage and record expected outcomes.
1. Update the version notices, command summary, offline Help summary and changelog together. Keep unimplemented ideas explicitly labelled.
1. Check screenshots against the current app. Use synthetic contacts, show the relevant UI state, and explain what readers should notice. Remove obsolete images from the guide rather than presenting them as current behaviour.
1. Build the site with `npm --prefix docs run build`, preview both guides, and check navigation links, diagrams, code examples and narrow-window layout. A successful build alone does not prove that diagram images rendered.
1. Ask a teammate to follow the Quick start without extra explanation. Use questions and documentation issues from the project tracker to improve unclear steps.

Keep the command summary before Quick start as the team's chosen reference layout; provide a direct Quick start link for new users.
