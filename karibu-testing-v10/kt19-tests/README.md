# Testing the framework itself

This project contains the tests which test the Karibu-Testing
framework itself (testing the testing itself - testing inception! :) .

This project contains only the test implementations -
the tests are actually run in separate projects which simulates
different environments. See the `kt10-testrun-*` projects for more details.

This project is internal to Karibu-Testing and is not published anywhere.
Under normal circumstances these would be simply placed in
`karibu-testing-v10/src/main/test` but that would prevent us from running
the tests in different environments.

This project tests only Vaadin 19-specific code - for generic Vaadin tests
please see the `kt10-tests` project.
