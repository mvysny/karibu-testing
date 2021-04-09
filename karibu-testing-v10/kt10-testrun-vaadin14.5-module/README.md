# Test Runner For Vaadin 14.5 tests with NPM+WebPack+Polymer 3

Runs all tests from the [kt10-tests](../kt10-tests) test battery.
Simulates a jar reusable component with the setup of
"Vaadin 14.5 + npm + webpack + Polymer 3".

The "-module" part means that this test doesn't test a WAR application,
but instead we test a reusable component which is supposed to be packaged
as jar, then used in an actual WAR app elsewhere.

The setup is activated by not having the Polymer 2 jar placed on the classpath.
Under normal circumstances you would have a `flow-build-info.json` file
placed in `resources/`, but that holds only for a WAR-type project.
If you have a jar module containing Vaadin 14.5 components, it doesn't need
to have `flow-build-info.json` and you still want to test using
the npm mode.

This project is internal to Karibu-Testing and is not published anywhere.

The jar project is actually able to publish `frontend/` folder in
`src/main/resources/META-INF/resources/frontend/`, so let's test that as well.

## Why Vaadin 14.5

It's good to test Karibu compatibility with older Vaadin versions as well, since
the customers may not use the latest&greatest and might conservatively choose
to use an older Vaadin version.
