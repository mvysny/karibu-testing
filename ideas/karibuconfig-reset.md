# Resetting KaribuConfig between tests

Status: **parked** (idea only, not scheduled). Split out of the F5 reload / unload-beacon work
(see the `2026-07-06` entry in [../DECISIONS.md](../DECISIONS.md)).

## The problem

`KaribuConfig` is a global (object) holding mutable test-tuning flags — e.g.
`fakeExtendedClientDetails`, `initDefaultRoute`, and the new `unloadBeaconTiming`. **Nothing resets
them between tests.** `MockVaadin.setup()` / `tearDown()` leave `KaribuConfig` untouched, so a flag a
test flips stays flipped for every later test in the JVM.

Today this is worked around per-flag in test code, e.g.:

```kotlin
@BeforeEach @AfterEach fun resetFakeExtendedClientDetails() { KaribuConfig.fakeExtendedClientDetails = true }
```

That's easy to forget and doesn't scale as more flags are added. It's a latent source of
order-dependent, flaky tests.

## Options

1. **Auto-reset in `tearDown()` (or `setup()`)** — reset `KaribuConfig` to defaults as part of the
   lifecycle. Cleanest for users; but it's a behavior change (a test that sets a flag *once* in
   `@BeforeAll` and relies on it surviving across methods would break) and must be documented.
2. **`KaribuConfig.reset()`** — an explicit method users can call in `@BeforeEach`. Non-breaking, opt-in,
   but still forgettable.
3. **Snapshot/restore around `setup`/`tearDown`** — capture on `setup()`, restore on `tearDown()`.
   Preserves a value set before `setup()` for the duration of that test only.

Leaning toward **1** (auto-reset on `tearDown`), possibly with **2** as the underlying implementation,
since defaults are the sane baseline and per-test flags are the norm. Needs an audit of every current
`KaribuConfig` field to confirm "reset to default" is always the right call.

## Interaction with the beacon feature

`unloadBeaconTiming` defaults to `EAGER`, which equals the old non-preserve behavior — so a leak
between tests is mostly *harmless* for that flag specifically, and the beacon feature does **not**
depend on this idea. But it adds one more flag to the pile, which is what motivates finally addressing
this. For now, beacon tests that flip the flag will reset it manually (per the existing convention).
