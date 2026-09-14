# Karibu-Testing — AGENTS.md

## What this is

The Unit Testing library for [Vaadin](https://vaadin.com/).

Karibu-Testing only runs in JVM and only tests server-side code. There is no browser running
and no JavaScript code running, which means that there is no communication between the browser and the server,
which means that you don't even need to run the servlet container. You simply create new instance
of your Vaadin form, modify the field values directly, then simulate a click to the "Save" button
and see the binder validations running. You can even call `UI.navigate()` to
navigate to a view; the navigation is handled completely server-side and adds components to `UI.getCurrent()`.

## Promises

- **Browserless and containerless.** Tests run in the JUnit JVM — no browser, no JavaScript, no servlet container; anything that needs one is out of scope.
- **We fake Vaadin's environment, never its behaviour.** Where Flow has a real code path, Karibu drives it; we emulate the outcome only where no path is reachable.
- **Kotlin-first with a first-class Java face.** Extension functions are the API surface; Java consumers get static mirrors, not a second-class subset.

## Design docs

| File | Owns | Loaded |
|---|---|---|
| `README.md` (root and per-module) | the pitch, and the user-facing book: how to *use* the library | — |
| `AGENTS.md` (this) | promises, invariants, the module map, conventions, commands | every turn |
| `design/architecture.md` | how the pieces compose — wiring, the lifecycle flows, the test battery; normative | lazy |
| `design/decisions.md` | why this and not that — `D_` entries, FAQ-shaped | lazy |
| `design/research.md` | what Vaadin, Spring and Gradle actually do — `R_` entries, each claim with provenance | lazy |
| `design/ideas/` | ideas not yet acted on, one per file; deleted once they ship | lazy |
| KDoc / Javadoc | what one symbol does and why it is shaped so | at the symbol |

Every fact lives in exactly one of these; the others link to it.

## Invariants

- **`MockVaadin` keeps the session, UI, request and response alive through `ThreadLocal` strong references.** Vaadin holds only soft refs, so dropping one lets a GC wipe the UI out mid-test.
- **`tearDown()` never calls `VaadinService.destroy()`.** That would also shut the service's executor down, killing reuse across tests. See `D_service_event_bus_compat`.
- **Anything reached inside Flow by reflection is probed at runtime, never assumed present.** A field that exists in `stable` may be gone in `next`. See `D_service_event_bus_compat`.
- **Vaadin is `compileOnly` in every published module.** A transitive Vaadin would silently override the version the consuming app picked.

## Module map

- `karibu-testing-v10` — the core: component helpers, locators, and the fake Vaadin plumbing under `mock/`.
- `karibu-testing-v23` — helpers for components that exist only from Vaadin 23 on.
- `karibu-testing-v24` — no production code; republishes `v23` and hosts the test runners.
- `karibu-testing-v10-groovy`, `karibu-testing-v10-pro-groovy` — Groovy extension modules.
- `karibu-testing-v10-spring` — Spring integration; `vaadin-spring` and `spring-security-core` compile-only.
- `karibu-testing-v10/tests`, `karibu-testing-v23/tests` — the test battery as an unpublished library.
- `karibu-testing-v24/testrun-{stable,next}-{webapp,module}` — the four environments that run the battery; see `design/architecture.md`.

## Conventions

- **Kotlin extension functions are the API surface.** Mirror a new helper in `LocatorJ` / `SearchSpecJ` wherever a Java-facing form makes sense.
- **Every published module sets `explicitApi()`.** A public declaration without an explicit visibility modifier fails the build.
- **Vaadin 25+ only, from 2.6.0 on.** The KT → Vaadin chart at the top of `karibu-testing-v10/README.md` outranks any surviving Vaadin-24 reference elsewhere.
- **Mind which Vaadin version you are reasoning about.** The library compiles against `stable` while the battery runs on both it and `next`; inspect the jar the failing test actually uses, not the one on the compile path.
- **A new test goes into `karibu-testing-v10:tests`** (or `karibu-testing-v23:tests` if it needs Vaadin 23+ APIs), never into a `testrun-*` project, or it runs in one environment instead of four.
- **Dependency versions live in `gradle/libs.versions.toml`**, never in an individual `build.gradle.kts`.
- **The `-SNAPSHOT` version is the next release number.** `2.7.3-SNAPSHOT` ships as `2.7.3`; use it when writing "since KT x.y" and don't bump it yourself. Releasing is `CONTRIBUTING.md`.

## Commands

- `./gradlew test` — the whole battery in all four environments.
- `./gradlew` — bare, this is `clean build`: the battery plus the doc tripwires. What CI runs.
- `./gradlew :karibu-testing-v24:testrun-stable-webapp:test` — one environment. Substitute another runner name.
- `./gradlew :karibu-testing-v24:testrun-stable-webapp:test --tests "AllTests.flow-build-info-json exists"` — one test.
- `design/verify_design_tripwires.sh` — the doc-layer checks alone.
- CI (`.github/workflows/gradle.yml`) runs that bare `./gradlew` on JDK 21 and 25 across Linux, macOS and Windows, plus a `design-docs` job for the tripwires. JDK 21 is the minimum, enforced in `build.gradle.kts`.

## Skills this project follows

- **KDoc carries each fact at the level of the symbol it describes**, and nothing the signature already says; the `writing-kdoc` skill has the rules.
- **A shipped idea is deleted, its lasting nuggets moved** to the homes in the *Design docs* table; the `ideas-folder` skill has the procedure.

## Maintenance of this file

Loaded every turn; cap 34 KB, a module's own `AGENTS.md` 10 KB. Over it, in this order:
delete what has no home — status, history, class lists, what the code already says; trim
each line to its fact plus one clause and send the explanation home — why →
`design/decisions.md`, how across symbols → `design/architecture.md`, how in one symbol →
its doc comment, what upstream does → `design/research.md`; only then a module's own
`AGENTS.md`, peripheral modules first, never the core. Never paraphrase a lazy entry into a
line here. `design/verify_design_tripwires.sh` checks the caps and the cites.
