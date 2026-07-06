# Multiple UIs (browser tabs) in one `VaadinSession`

Status: **parked** (idea only, not scheduled). Motivated by a downstream "Vaadin tab scope" library
(see the `2026-07-06` entry in [../DECISIONS.md](../DECISIONS.md)).

Last brainstormed: 2026-07-06. Nothing below is implemented.

Depends on [configurable-window-name.md](configurable-window-name.md) — a second tab is only
meaningful if it can carry a *distinct* `window.name`.

## The problem

`MockVaadin.setup()` creates exactly **one** `UI` in the session, and `createUI(...)` is `internal`.
There is no public way to add a **second UI** — i.e. a second browser tab — to the *same*
`VaadinSession`. Reload replaces the single UI; it never gives you two live tabs side by side.

So anything that is fundamentally *per-tab* cannot be tested: today every Karibu test is a
single-tab world.

## What real Flow does

One `HttpSession`/`VaadinSession` backs many tabs of the same app in the same browser. Each tab has
its own `UI` and its own `window.name` (via `ExtendedClientDetails`); `VaadinSession.getUIs()`
returns all of them. UIs share session-scoped state but are otherwise independent — one tab
navigating or closing does not disturb another.

The existing `reloadCurrentUI()` already demonstrates the machinery in-repo: it juggles multiple UIs
in one session (fresh `uiId` so `addUI` doesn't evict the other), flips `UI.getCurrent()`, and drives
Flow's real init/detach. A second-tab helper is the same moves without discarding the original.

## Proposed API

- **`MockVaadin.newBrowserTab(windowName: String = <distinct default>): UI`** — creates a second UI
  in the *current* `VaadinSession` with a distinct faked ECD (`windowName`), gives it a fresh `uiId`
  (so it coexists with the existing UI rather than evicting it), fires the real UI-init listeners,
  navigates it to the default route, and makes it `UI.getCurrent()`. Returns the new UI.
- **`MockVaadin.switchToTab(ui: UI)`** (or reuse `UI.setCurrent` + session current) — make an
  existing UI the current one, so a test can act "in tab A", then "in tab B", then back.

Both mirror `reloadCurrentUI`/`createUI` internals; the main new work is *not* discarding the old UI
and giving tests a handle to switch focus. `tearDown()` must clean up all UIs, not just the current.

Open question: whether closing a tab (`MockVaadin.closeTab(ui)`) belongs here or is just
`ui.close()` + the existing end-of-request cleanup. Probably the latter; document it.

## Motivating consumer

`vaadin-tab-scope` — this is the capability its entire reason-to-exist rests on, and the one thing it
currently *cannot* test at all:

- two tabs (two `window.name`s) → **two independent** `TabScope`s;
- a value stored in tab A is **not** visible in tab B (no leakage);
- orphaning/closing tab A does **not** destroy tab B's scope (independent lifecycle);
- a `@TabScoped` route resolves to a **different** instance per tab.

Combined with [../ideas/heartbeat-emulation.md](heartbeat-emulation.md) (idle-UI reaping), this also
lets the `UnloadBeaconTiming.NEVER` lingering-UI case be driven to its eventual reap.

## Handoff when it ships

Delete this file, add a dated `DECISIONS.md` entry (rationale + why we don't model closing specially),
and KDoc the new helpers.
