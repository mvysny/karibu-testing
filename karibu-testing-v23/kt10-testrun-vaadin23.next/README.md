# Test Runner For Vaadin 23 tests

Runs all tests from the [kt10-tests](../kt10-tests) test battery.
Simulates a WAR app with the setup "Vaadin 23 + pnpm + webpack".

The setup is activated by having
a specially placed [flow-build-info.json](src/test/resources/META-INF/VAADIN/config/flow-build-info.json)
file. Under normal circumstances this file is generated by Maven Vaadin plugin,
but in this project we pretend that it has been already generated.

This project is internal to Karibu-Testing and is not published anywhere.