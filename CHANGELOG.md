# Changelog

All notable changes to Karibu-Testing are documented here. The format is based on
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Baseline: 2.7.0. Changes prior to 2.7.0 are not documented here.

## [Unreleased] (2.7.3)

### Fixed

- Discover `Dialog` header/footer children on Vaadin 25.3.

## [2.7.2] - 2026-07-21

### Added

- Open/click a `ContextMenu` via its target component (#20).
- `Grid._doubleClickItem()` column overloads.
- Spring Security support via `MockSpringSecurity.mock()` (#94).
- Browser-free login helper `MockVaadin.login()`/`logout()` (#143).
- Assert on `LitRenderer` HTML via JSoup (#175).

### Fixed

- Route `GridContextMenu.setOpened()` through the real DOM event (#20).
- Follow the Grid item-details `DataGenerator` move in Vaadin 25.3.
- Route unload-beacon simulation through the real `ServerRpcHandler` (#210).

### Dependencies

- Vaadin 25.2.3, Kotlin 2.4.10.

## [2.7.1] - 2026-07-06

### Added

- `MockBrowser` — multiple browser tabs per `VaadinSession`.
- `MockVaadin.reapInactiveUIs()` to reap lost-beacon UIs on F5.
- Configurable unload-beacon timing on F5 reload (non-`@PreserveOnRefresh`).
- Higher-fidelity F5 / `@PreserveOnRefresh` lifecycle in `MockPage.reload()` (#207).
- `@JvmOverloads` on `clientRoundtrip()` for Java backward compatibility (#206).

### Dependencies

- Vaadin 25.2.1, Kotlin 2.4.0, karibu-dsl 2.6.0, JUnit 6.0.3, Gradle 9.5.1.
