# Test Runner For Vaadin 14 tests with NPM+WebPack+Polymer 3

Runs all tests from the [kt10-tests](../kt10-tests) test battery.
Simulates a Vaadin 14 + npm + webpack + Polymer 3 setup.

The setup is activated by not having the Polymer 2 jar placed on the classpath.
Under normal circumstances you would have a `flow-build-info.json` file
placed in `resources/`, but that holds only for a WAR-type project.
If you have a jar module containing Vaadin 14 components, it doesn't need
to have `flow-build-info.json` and you still want to test using
the npm mode.

This project is internal to Karibu-Testing and is not published anywhere.

The jar project is actually able to publish `frontend/` folder in
`src/main/resources/META-INF/resources/frontend/`, so let's test that as well.
