# Testing the framework itself

This project contains the tests which test the Karibu-Testing
framework itself (testing the testing itself - testing inception! :) .

This project contains only the test implementations -
the tests are actually run in separate projects which simulates
different environments:

* [kt10-testrun-vaadin13](../kt10-testrun-vaadin13) which runs the tests
  in Vaadin 13 + Bower + WebJars + Polymer 2
* [kt10-testrun-vaadin14](../kt10-testrun-vaadin14) which runs the tests
  in Vaadin 14 + Bower + WebJars + Polymer 2 (so-called Compatibility mode)
* [kt10-testrun-vaadin14-npm](../kt10-testrun-vaadin14-npm) which runs the tests
  in Vaadin 14 + NPM + WebPack + Polymer 3
* [kt10-testrun-vaadin15](../kt10-testrun-vaadin15) which runs the tests
  in Vaadin 15

This project is internal to Karibu-Testing and is not published anywhere.
Under normal circumstances these would be simply placed in
`karibu-testing-v10/src/main/test` but that would prevent us from running
the tests in different environments.
