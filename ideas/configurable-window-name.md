# Configurable `window.name` (browser tab identity)

Status: **parked** (idea only, not scheduled). Motivated by a downstream "Vaadin tab scope" library
(see the `2026-07-06` entry in [../DECISIONS.md](../DECISIONS.md), which already names it as the
beacon-timing consumer).

Last brainstormed: 2026-07-06. Nothing below is implemented.

## The problem

The faked `ExtendedClientDetails.windowName` is a **hardcoded constant** —
`createExtendedClientDetails(..., windowName: String = "ROOT-2521314-0.2626611481", ...)` in
`Utils.kt` — and the auto-fake path (`KaribuConfig.fakeExtendedClientDetails`, applied in
`MockPage.retrieveExtendedClientDetails`) always uses that default. So:

- **Every** UI and **every** session in a JVM gets the *same* `window.name`.
- The `windowName` parameter on `createExtendedClientDetails` exists but is **not plumbed through**
  `MockVaadin.setup()` / the auto-fake path — a test cannot pick the tab id.
- On an F5 `Page.reload()` the faked ECD is re-created with the same constant, so `window.name` is
  always preserved across reload.

`window.name` is *the* browser primitive for tab identity, and it's exactly what a tab-scope library
keys on. Being unable to vary it means two whole classes of behavior are untestable (see below).

## What real Flow / the browser does

`window.name` is a browser-window property, surfaced to the server as `ExtendedClientDetails
.getWindowName()` (Flow sends it as the `v-wn` param; since Flow 25.2 on the bootstrap request
itself). Two realities we can't currently model:

1. **Distinct tabs have distinct `window.name`s** within one `VaadinSession` (prerequisite for
   [multiple-uis-per-session.md](multiple-uis-per-session.md)).
2. **Some browsers do not preserve `window.name` across a reload.** Notably Safari 18.3.1 with dev
   tools closed, and navigation via typed URL / bookmark — the reloaded page presents a *fresh*
   `window.name`, so the server sees a brand-new tab (see
   [vaadin/flow#21141](https://github.com/vaadin/flow/issues/21141)). Karibu always preserves it, so
   this failure mode is invisible.

## Proposed API

Small, additive, defaults preserve today's behavior:

- **`KaribuConfig.windowName: String`** (default `"ROOT-2521314-0.2626611481"`) — the value the
  auto-fake ECD path uses. Setting it before `MockVaadin.setup()` fixes a test's tab id. (Subject to
  the same reset caveat as every other flag — see [karibuconfig-reset.md](karibuconfig-reset.md).)
- **Changing `window.name` on reload.** Model the non-preserving browsers. Either:
  - a one-shot `MockVaadin.reloadWithNewWindowName(windowName: String)` helper, or
  - honor a changed `KaribuConfig.windowName` on the next `Page.reload()` (the reloaded UI's faked
    ECD picks up the current config value rather than the old UI's).

  The one-shot helper is clearer about intent ("this reload lost the tab id"); the config approach is
  fewer entry points. Lean helper.

A `windowName` factory (`() -> String`) is overkill unless a test needs many auto-distinct tabs —
defer until [multiple-uis-per-session.md](multiple-uis-per-session.md) actually needs it.

## Motivating consumer

`vaadin-tab-scope`:

- **Tab-identity fragility test** — reload that changes `window.name` must arrive as a *new* tab
  scope (old one orphans), matching the documented Safari/bookmark behavior (its INTERNALS.md, "Tab
  identity fragility").
- **Foundation for multi-tab** — two distinct `window.name`s are the precondition for testing that
  two browser tabs get two independent scopes.

## Handoff when it ships

Per this repo's convention: delete this file, add a dated `DECISIONS.md` entry (rationale + the
reset-interaction note), and cover the mechanics in KDoc on the new config/helper.
