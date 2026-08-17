# Test Runner: stable Vaadin, jar-module app

Runs [the core test battery](../../karibu-testing-v10/tests) against the
pinned stable Vaadin version (`vaadin` in `libs.versions.toml`).
Simulates a jar reusable component.

The "-module" part means that this test doesn't test a WAR application,
but instead we test a reusable component which is supposed to be packaged
as jar, then used in an actual WAR app elsewhere.

The setup is activated by not having the Polymer 2 jar placed on the classpath.
Under normal circumstances you would have a `flow-build-info.json` file
placed in `resources/`, but that holds only for a WAR-type project.
If you have a jar module containing Vaadin 24 components, it doesn't need
to have `flow-build-info.json` and you still want to test using
the npm mode.

This project is internal to Karibu-Testing and is not published anywhere.

The jar project is actually able to publish the `frontend/` folder from the classpath,
so let's test that as well - both locations:

* `src/main/resources/META-INF/frontend/` - what the Vaadin 25 docs tell add-ons to use;
* `src/main/resources/META-INF/resources/frontend/` - the Vaadin 24 location, deprecated
  in Vaadin 25 but still supported.
