# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

Karibu-Testing is a browserless, containerless unit-testing library for Vaadin Flow. Tests run in the same JVM as JUnit — there is no servlet container, no browser, no JavaScript. `MockVaadin.setup()` fabricates `VaadinSession`, `UI`, `CurrentRequest`, etc., then tests call server-side Vaadin APIs directly (`UI.navigate()`, `_click()`, locator functions) and assert on the server-side component tree.

The library is Kotlin-first and leans on Kotlin extension functions; Java consumers go through static helper classes (`LocatorJ`, `SearchSpecJ`); Groovy has its own modules.

## Documentation — where each kind of doc lives

Four homes, by audience and lifecycle. Put content where it belongs and don't duplicate across them:

- **`README.md` (per-module, scattered)** — user-facing "book" prose: how to *use* the library. Kept current.
- **Code KDoc/Javadoc** — the authoritative "how it works now" (mechanics). Lives with the code so it can't drift; the single source of truth for current behavior. Don't restate mechanics in standalone docs.
- **`ideas/`** — forward-looking design notes, one markdown file per non-trivial idea, brainstormed before/alongside implementation. **Deleted once implemented** (ideas are forward-looking only).
- **`DECISIONS.md`** — append-only, dated log of technical decisions and their *rationale* (the "why", alternatives rejected, evidence). Permanent; never edit an old entry — supersede it with a new one.

Handoff when an idea ships: delete its `ideas/` file, record the rationale as a new `DECISIONS.md` entry, and ensure the mechanics are covered by code KDoc.

## Build / test commands

- `./gradlew test` — run all tests across all Vaadin-variant test-runner modules. This is the CI command (see `.github/workflows/gradle.yml`).
- `./gradlew build` — default task chain is `clean`, `build`.
- `./gradlew :karibu-testing-v24:testrun-stable-webapp:test` — run the stable-Vaadin test battery only. Substitute the module name for other variants.
- `./gradlew :karibu-testing-v24:testrun-stable-webapp:test --tests "AllTests.flow-build-info-json exists"` — run a single test.
- JDK 21 is the minimum (enforced in `build.gradle.kts`). CI matrix covers JDK 21 and 25 on Linux/macOS/Windows.
- Dependency versions (Vaadin, Kotlin, JUnit, kaributools, etc.) live in `gradle/libs.versions.toml` — edit there, not in individual `build.gradle.kts` files.

## Release process

See `CONTRIBUTING.md`. The current `-SNAPSHOT` version *is* the next release number: `2.7.2-SNAPSHOT`
ships as `2.7.2` (or `2.8` for a major change). Use that when writing "since KT x.y" docs — don't bump it yourself.

## Module layout

The project is one Gradle multi-module build. Published artifacts live under group `com.github.mvysny.kaributesting`:

- `karibu-testing-v10` — **core**. Vaadin 14+ compatible. All the component testing helpers (`Button.kt`, `Grid.kt`, `ComboBox.kt`, `Locator.kt`, `MockVaadin.kt`, `Routes.kt`, …) live here under package `com.github.mvysny.kaributesting.v10`. The `mock/` subpackage contains the fake Vaadin plumbing (`MockVaadinServlet`, `MockVaadinSession`, `MockService`, `MockNpmTemplateParser`, …). Java-facing API is in `src/main/java/.../LocatorJ.java` and `SearchSpecJ.java`. The `pro/` subpackage covers Grid Pro / ConfirmDialog (Vaadin Pro components).
- `karibu-testing-v23` — thin layer on top of v10 adding helpers for components that only exist in Vaadin 23+ (`VirtualList`, `MultiselectComboBox`, `SideNav`, tabs extras). Package `com.github.mvysny.kaributesting.v23`.
- `karibu-testing-v24` — **has no production code**; it exists as a container for the test-runner subprojects (see below) and republishes `karibu-testing-v23` so users get a stable Maven coordinate aligned to Vaadin 24. **Module naming rule:** the `vNN` suffix marks the *highest Vaadin version whose version-specific APIs the module supports* — not the Vaadin version it runs against. `karibu-testing-v24` supports Vaadin APIs up to and including Vaadin 24 and nothing Vaadin 25+-specific; it still works fine *running* on Vaadin 25. The day we need to support a Vaadin 25+-specific API, we add a new `karibu-testing-v25` module rather than renaming this one. This coordinate is **published, so it never gets renamed.**
- `karibu-testing-v10-groovy`, `karibu-testing-v10-pro-groovy` — Groovy extension modules.
- `karibu-testing-v10-spring` — Spring integration (`SpringInstantiator` hooks, etc.). Only a compile-only dependency on `vaadin-spring`.

## How the test layout works (important)

Tests for the library itself do **not** live next to the code they test. Instead:

- `karibu-testing-v10/tests` and `karibu-testing-v23/tests` are **test-source libraries** — they contain test classes but no `src/test`, only `src/main`. They are internal (not published). (Their module *directory* is `tests`; the parent `-v10`/`-v23` carries the Vaadin baseline.)
- The real test execution happens in `karibu-testing-v24/testrun-*` projects, each of which depends on `karibu-testing-v23:tests` and runs the whole battery against a different simulated environment. The name encodes two axes — `{stable|next}` Vaadin version × `{webapp|module}` packaging:
  - `testrun-stable-webapp` — WAR/webapp-style app, stable Vaadin (the pinned `vaadin` version in `libs.versions.toml`), with `flow-build-info.json` on the classpath.
  - `testrun-stable-module` — jar-module-style app (no `flow-build-info.json`), stable Vaadin.
  - `testrun-next-webapp` — WAR/webapp-style, Vaadin "next" (the newer prerelease pinned as `vaadin_next`).
  - `testrun-next-module` — jar-module-style, Vaadin next.

Consequence: when you add a new test, put it in `karibu-testing-v10:tests` (or `karibu-testing-v23:tests` if it needs Vaadin 23+ APIs), and it will automatically run in every environment. When debugging an environment-specific failure, run the specific `testrun-*` module.

**Caveat — mind which Vaadin version you're reasoning about vs. testing against.** The library *compiles* against `libs.vaadin.stable.all` (one pinned version, currently 25.x), but the tests *run* against several different Vaadin versions (24 LTS and `vaadin_next`), and Vaadin's own class hierarchies shift between them. For example, `TextRenderer` and `ComponentRenderer` **extend `LitRenderer` in Vaadin 24** but **not** in Vaadin 25. So when you inspect a Vaadin jar / `javap` / source to predict behavior, confirm it's the *same* version the failing test actually runs with — a 25.x jar will mislead you about a Vaadin-24 test run, and vice versa.

## API conventions

- All published modules set `kotlin { explicitApi() }` — every top-level/public declaration must carry an explicit visibility modifier (`public`, `internal`, etc.). Gradle build fails otherwise.
- Kotlin extension functions are the primary API surface (e.g. `Button._click()`, `Grid._size()`, `HasValue._value=`). Mirror new Kotlin helpers in `LocatorJ` / similar Java helpers when a Java-facing equivalent makes sense.
- `MockVaadin` holds strong refs to the session/UI via `ThreadLocal` because Vaadin only keeps soft refs — don't change that without understanding why it's there.
- `TestingLifecycleHook` is the extension point for plugging in custom test setup/teardown behavior (consult it before adding global mutable state).

## Dependency notes

- v10 and v23 use `compileOnly(libs.vaadin.v24.all)` — the library does **not** pull Vaadin transitively; the consuming app picks its Vaadin version. Preserve this pattern when adding dependencies.
- `fake-servlet5` (separate library by the same author) provides the `jakarta.servlet` fakes — we don't roll our own.
- `kaributools` / `kaributools23` (separate libraries) provide general Vaadin utilities (e.g. `VaadinVersion`) that Karibu-Testing builds on.
