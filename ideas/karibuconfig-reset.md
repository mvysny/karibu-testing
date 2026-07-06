# Resetting KaribuConfig between tests

Status: **parked** (idea only, not scheduled). Split out of the F5 reload / unload-beacon work
(see the `2026-07-06` entry in [../DECISIONS.md](../DECISIONS.md)).

Last brainstormed: 2026-07-06. This file records what we ruled out and *why*, so we don't
re-tread it when this is picked up. Nothing below is implemented.

## The problem

`KaribuConfig` is a global (object) holding mutable test-tuning flags — e.g.
`fakeExtendedClientDetails`, `initDefaultRoute`, and the new `unloadBeaconTiming`. **Nothing resets
them between tests.** `MockVaadin.setup()` / `tearDown()` leave `KaribuConfig` untouched, so a flag a
test flips stays flipped for every later test in the JVM.

Today this is worked around per-flag in test code, and the workarounds are already scattered across
the suite — each one a place someone can forget:

- `MockVaadinTest.kt` — `@BeforeEach @AfterEach fun resetFakeExtendedClientDetails() { … = true }`
- `LitTemplateTest.kt` — `@AfterEach fun resetConfig() { …includeVirtualChildrenInTemplates = false }`
- `LocatorTest.kt` — `@AfterEach { KaribuConfig.testingLifecycleHook = TestingLifecycleHook.default; … }`
- the beacon tests flip `unloadBeaconTiming` back to `EAGER` by hand

It's easy to forget and doesn't scale as more flags are added. It's a latent source of
order-dependent, flaky tests. Worth fixing — but *how* is subtler than it first looks.

## Options considered

### Option 1 — auto-reset every field to its default (in `tearDown()`). REJECTED.

Clean mental model ("every test starts pristine"), but it silently breaks any field a user configures
**once, globally** (static initializer / `@BeforeAll`): after the first test's `tearDown()` the field
reverts to its default and every later test loses the setting. That's the *same* order-dependent
flakiness we're trying to kill, just relocated to a different field.

The decisive example is `testingLifecycleHook`: the KDoc (`TestingLifecycleHook.kt:54`) itself
promotes installing a custom hook once for the whole suite. Blanket reset-to-default would revert it
after test #1. And — crucially — the problem is **not** limited to the obvious "wiring" fields:
`defaultIsFromClient` (a compatibility switch, meant to be chosen once per suite) and `initDefaultRoute`
can also legitimately be set-once-global. See "The fuzzy slash" below.

### Option 2 — explicit `KaribuConfig.reset()` users call in `@BeforeEach`. Kept as a building block.

Non-breaking, opt-in, but forgettable on its own. Still useful as the *underlying implementation* that
`tearDown()` calls (and that users can call directly if they want per-test scrubbing without relying
on the lifecycle).

### Option 3 — snapshot at `setup()`, restore at `tearDown()`. REJECTED.

Tempting because it's symmetric and preserves set-once-global config (each `setup()` re-snapshots the
ambient baseline). But its restore *target* is "whatever arbitrary state config happened to be in at
the matching `setup()`" — not a nameable constant. Worse, under re-entrant
`setup()/tearDown()/setup()/tearDown()` the post-`tearDown()` state depends on which `setup()`'s
snapshot is being restored, so you can't state in one sentence what config looks like after teardown.
A reset convention whose result you can't name isn't a reset convention. Out.

### Option 4 — scoped `reset()` to solid defaults, called from `tearDown()`. CURRENT LEAN.

Keep the reliable, nameable contract ("after `tearDown()`, the reset-scoped fields are at their
documented defaults") but **scope the reset to per-test tuning flags** and leave environment-wiring
fields untouched. Implemented as a public `KaribuConfig.reset()` (folding in Option 2) that
`tearDown()` invokes.

Refinement discussed: introduce a separate holder — a **`KaribuSPI`** — for the wiring fields
(`testingLifecycleHook`, `pendingJavascriptInvocationHandlers`), keep the old `KaribuConfig`
properties as **deprecated forwarders**, and have `reset()` cover *everything remaining* in
`KaribuConfig`. Then "`reset()` resets all of `KaribuConfig` to defaults" becomes literally true with
no asterisk — the exceptions live in a different class by construction, not as a documented carve-out.

## Why the reset must be anchored at `tearDown()`, not `setup()`

- **`setup()`-start is impossible.** Several fields are *consumed during* `setup()` and must honor the
  value the user set on the line right before it: `initDefaultRoute` (read in `createUI`),
  `fakeExtendedClientDetails` and `testingLifecycleHook` (read via `createUI` → `clientRoundtrip`).
  Resetting at the start of `setup()` would clobber exactly that pre-`setup()` configuration
  (e.g. `MockVaadinTest`'s `fakeExtendedClientDetails=false` test).
- **`tearDown()` (after the test) is the only anchor compatible with pre-`setup()` config.** Downside:
  `tearDown()`'s own KDoc currently advertises it as *skippable* ("you don't have to call this
  function"). If we hang the reset on it, we must update that guidance — for config hygiene it is no
  longer optional. Skipping it just means today's leaky behavior (no regression, only a missed
  improvement). Every test in this repo already calls it, so in practice it's universal.

## The fuzzy slash (the core unsolved problem)

"Per-test tuning" vs. "set-once environment" is **not a clean property of a field** — it's a property
of *how a given app chooses to configure it*. The same field lives in both worlds depending on the
app. So any fixed categorization the library ships will be wrong for some apps. `defaultIsFromClient`
is the poster child: framed by its own KDoc as a suite-wide compatibility choice, yet nothing stops a
test flipping it per-method.

Consequence: Option 4's field categorization is inherently a *judgement call with residual breakage*,
not a provably-correct partition. That's acceptable given "predictability > backward-compat", but it
must be documented, and the convention taught: **global baselines for reset-scoped tuning flags belong
in `@BeforeEach` (before `setup()`), not in a static initializer.** Set them there and they survive,
because `@BeforeEach` re-applies them every test.

## Field-by-field audit (first pass — revisit before implementing)

| Field | Default | Lean | Notes / doubts |
|---|---|---|---|
| `includeVirtualChildrenInTemplates` | `false` | reset (test-scoped) | This repo resets it per-test already. An app that always wants virtual children would set it per-`@BeforeEach`. |
| `fakeExtendedClientDetails` | `true` | reset (test-scoped) | "Expert setting." Consumed during `setup()`. Flipped per-test in this repo. |
| `defaultIsFromClient` | `true` | **contested** | Compat switch, KDoc frames it as suite-wide ("emulate previous versions"). Suite-scoped *and* per-test flippable — the fuzzy slash in one field. |
| `initDefaultRoute` | `true` | **contested** | Consumed during `setup()`. Per-test in this repo, but an app could reasonably set it once globally. |
| `unloadBeaconTiming` | `EAGER` | reset (test-scoped) | Per-test F5 scenarios. Leak is mostly harmless (default == old behavior) but it's one more flag on the pile. |
| `testingLifecycleHook` | `.default` | **do not reset → move to `KaribuSPI`** | Wiring. KDoc promotes install-once-globally. |
| `pendingJavascriptInvocationHandlers` | empty list | **do not reset → move to `KaribuSPI`** | Wiring. Also raises mutable-collection reset semantics (clear-in-place vs. replace-reference). |

## Open topics to resolve when this is picked up

1. **Is `KaribuSPI` the right cut?** Moving `testingLifecycleHook` + `pendingJavascriptInvocationHandlers`
   out (with deprecated forwarders left in `KaribuConfig`) makes the reset scope self-evident, but it's
   a breaking-ish API change. Confirm the deprecation path and whether the forwarders themselves are
   excluded from reset cleanly.
2. **The contested fields** (`defaultIsFromClient`, `initDefaultRoute`): reset them (predictability,
   accept the static-init residual) or leave them alone (treat as suite config)? No clean answer — this
   is the fuzzy slash. Decide per-field, document the residual.
3. **Mutable-collection semantics** for `pendingJavascriptInvocationHandlers` if it ever *were* reset:
   clear-in-place vs. replace-reference (aliasing bugs lurk here). Moot if it moves to `KaribuSPI` and
   is never reset — which is another argument for the move.
4. **`tearDown()` KDoc** must drop the "you don't have to call this" framing (at least for config
   hygiene) if the reset hangs off it.
5. **Future-proofing:** every *new* `KaribuConfig` field will force this per-test-vs-suite judgement
   again. Consider documenting the default expectation (new fields are reset-scoped unless they're
   wiring → then they go in `KaribuSPI`) so the decision isn't re-litigated ad hoc.

## Interaction with the beacon feature

`unloadBeaconTiming` defaults to `EAGER`, which equals the old non-preserve behavior — so a leak
between tests is mostly *harmless* for that flag specifically, and the beacon feature does **not**
depend on this idea. But it adds one more flag to the pile, which is what motivates finally addressing
this. For now, beacon tests that flip the flag reset it manually (per the existing convention).
