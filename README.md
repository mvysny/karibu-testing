[![CI](https://github.com/mvysny/karibu-testing/actions/workflows/gradle.yml/badge.svg)](https://github.com/mvysny/karibu-testing/actions/workflows/gradle.yml)
[![Join the chat at https://gitter.im/vaadin/vaadin-on-kotlin](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/vaadin/vaadin-on-kotlin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![GitHub tag](https://img.shields.io/github/tag/mvysny/karibu-testing.svg)](https://github.com/mvysny/karibu-testing/tags)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.mvysny.kaributesting/karibu-testing-v8/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.mvysny.kaributesting/karibu-testing-v8)

# Karibu-Testing: The Vaadin Unit Testing

The Unit Testing library for [Vaadin](https://vaadin.com/).

Karibu-Testing gives you the ability to call `UI.getCurrent()` straight from your JUnit
test methods, and receive a
meaningful result in the process. You can call `UI.navigate()` to navigate around in your app;
you can instantiate your components and views directly from your JUnit test methods.
In order to do so, Karibu-Testing mocks `CurrentRequest`, `VaadinSession` and others in your
currently running JVM (in which your JUnit tests run).

Karibu-Testing removes the necessity to run
both the browser and the servlet container, in order to test your Vaadin-based apps
insanely fast.

1. *containerless testing*: You don't need to launch the servlet container.
Karibu-Testing creates Vaadin Session, the UI and other necessary Vaadin classes
straight in the JVM which runs your JUnit tests.
2. *browserless testing*: You look up components straight from
`UI.getCurrent()`, bypassing browser and the JavaScript->Server bridge completely.
You directly call methods on your server-side View classes and on the server-side Java Vaadin components.

> If you like the library, please star it. The more stars, the more popularity and more maintainenance the library will receive.

Check out a [30 second video of live coding with Karibu-Testing](https://www.youtube.com/watch?v=znVNEo9cj98) to get a taste
on how simple this library is.

The library supports Kotlin, Java and Groovy (Groovy support for Vaadin 14+ only).

## Getting Started / Main Documentation / Full Documentation

### Vaadin 14+

**>>>See [The main Karibu-Testing documentation for Vaadin 14+](karibu-testing-v10) <<<**.
Karibu-Testing is compatible with any Vaadin 14+ version (14, 15, 16, 17, 18, 19, 20, 21, 22, 23 etc).

### Vaadin 8

**>>>See [The main Karibu-Testing documentation for Vaadin 8](karibu-testing-v8) <<<**. Karibu-Testing is compatible with any Vaadin 8.x version.

## Why Unit-testing?

Advantages over the traditional testing with [Selenium](https://www.seleniumhq.org/)/[TestBench](https://vaadin.com/testbench):

* *Fast*: Browserless tests are typically 100x faster than Selenium-based tests and run in 5-60 milliseconds,
  depending on their complexity.
* *Reliable*: We don't need arbitrary sleeps since we're server-side and we can await until the request is fully processed.
  We don't use Selenium drivers which are known to fail randomly.
* *Headless*: The tests run headless since there's no browser. There is no need to setup screen in
  your CI environment.
* *Simple*: the test runs in the same JVM as the server. You start the server in your `@BeforeClass` and
  stop the server in your `@AfterClass`. There is no need to use Maven's Integration plugin
  to start the server in the background (and then remember to kill it afterwards, otherwise all future CI tests
  will fail to start the server since it's already running).
* *Robust*: the test runs in the same JVM as the server. If the
  server fails to start and throws an exception, the test method too will fail with the same exception.
  No need to go hunting for exceptions in a log located somewhere on a CI server.

With this technique you can run 600 UI tests in 7 seconds, as opposing to 1-2 hours with Selenium-based approach.
Because of the speed, you can let the UI tests run after every commit by your continuous integration server.

Since we're bypassing the browser and talking to Vaadin server API directly, you don't even need to start the servlet container -
you can just add the server jars onto testing classpath and call Vaadin server API which will in turn invoke your server logic.

A 15-minute [video](https://www.youtube.com/watch?v=XOhv3y2GXIE) explains everything behind the browserless testing technique.

## Kotlin, Java and Groovy support

The Karibu-Testing library is Standalone and Fully Supports Java and Groovy.

Even though the Karibu-Testing is endorsed by [Vaadin-on-Kotlin](http://vaadinonkotlin.eu), the Karibu-Testing
does not really depend on any other technology than Vaadin. You don't have to use Vaadin-on-Kotlin nor Karibu-DSL to use Karibu-Testing.
You don't even need to write your app nor your tests in Kotlin, since the library provides native API
for Kotlin, Java and Groovy.

You can therefore plug this library in into your Java+Vaadin-based project as a test dependency, and write the test code
in Java, Kotlin or Groovy, whichever suits you more.

Karibu-Testing is published on Maven Central, so it's very easy to add as a Maven dependency.

## Project Status

The library is mature and stable since 2020. It has been extensively tested and employed in numerous
real-world projects. Small features are being added infrequently,
and compatibility with all latest Vaadin versions is checked on every commit.

## Example Projects

A list of a very simple example projects that employ Karibu Testing:

* Vaadin 8 + Kotlin + Gradle + [DynaTest](https://github.com/mvysny/dynatest): [karibu-helloworld-application](https://github.com/mvysny/karibu-helloworld-application)
* Vaadin 8 + Java + Maven + JUnit 5: [vaadin8-sqldataprovider-example](https://github.com/mvysny/vaadin8-sqldataprovider-example)
* Vaadin 21 + Kotlin + Gradle + [DynaTest](https://github.com/mvysny/dynatest): [karibu10-helloworld-application](https://github.com/mvysny/karibu10-helloworld-application)
* Vaadin 14 + Java + Maven + JUnit 5: [skeleton-starter-kt](https://github.com/mvysny/skeleton-starter-kt)
* Vaadin 14 + Java + Maven + JUnit 5: [vaadin10-sqldataprovider-example](https://github.com/mvysny/vaadin10-sqldataprovider-example)
* Vaadin 8 + Spring Boot + Java + Maven + JUnit 4: [karibu-testing-spring](https://github.com/mvysny/karibu-testing-spring)
* Vaadin 21 + Spring Boot + Java + Maven + JUnit 5: [t-shirt-shop-example](https://github.com/mvysny/t-shirt-shop-example)
* Vaadin 14 + Vaadin-on-Kotlin + Gradle + DynaTest: [Bookstore Example](https://github.com/mvysny/bookstore-vok)
* Vaadin 14 + Vaadin-on-Kotlin + Gradle + DynaTest: [Beverage Buddy VoK](https://github.com/mvysny/beverage-buddy-vok)
* Vaadin 14 + Vaadin-on-Kotlin + Gradle + DynaTest: [Vaadin Kotlin PWA](https://github.com/mvysny/vaadin-kotlin-pwa)
* Vaadin 14 + Java + Quarkus + Maven: [vaadin-quarkus](https://github.com/mvysny/vaadin-quarkus) (also check the `v19` branch for Vaadin 19).
* Vaadin 21 + Java + Spring Security + Maven: [vaadin-spring-karibu-testing](https://github.com/mvysny/vaadin-spring-karibu-testing)

## Limitations

Karibu-Testing is designed to bypass browser and the servlet container. It's a double-edged
sword: while this provides insane speed, it also sets limits what you can do with Karibu-Testing:

* There is no browser: **It's not possible to test nor call JavaScript code**. That also limits
  testability of your Polymer Templates; see [Testing with Vaadin](karibu-testing-v10) for more details.
  If you need to test JavaScript code, you need to use Selenium or [TestBench](https://vaadin.com/testbench) in
  addition to Karibu-Testing.
* Since your JUnit methods access views which in turn access your business logic beans
  and your database directly, **You must be able to "start" your app in the same JVM which runs the tests.**.

Here are a few tips on how to "start" your app in the JUnit JVM:

* For simple apps with just the UI and no database that's very easy: simply call `MockVaadin.setup { MyUI() }` before every test, and `MockVaadin.tearDown()` after every test. That will
instantiate your UI along with all necessary Vaadin environment.
* For more complex apps employing database access (for example [Vaadin-on-Kotlin](http://vaadinonkotlin.eu) apps) you need to bootstrap Vaadin-on-Kotlin. Luckily that's very easy,
simply configure your database in `VaadinOnKotlin.dataSourceConfig` and then init vok: `VaadinOnKotlin.init()` before all tests. Or even better,
since you typically do this in a `ServletContextListener` such as the `Bootstrap` class, simply call that: `Bootstrap().contextInitialized(null)`. Then, when the app is bootstrapped,
you can proceed to setting up your UI by calling `MockVaadin.setup { MyUI() }` before every test, and `MockVaadin.tearDown()` after every test.
* For more complex apps not using Vaadin-on-Kotlin, just use the same approach of simply calling all `ServletContextListener` which you have in your app, before all tests are executed.
Then, when the app is bootstrapped,
you can proceed to setting up your UI by calling `MockVaadin.setup { MyUI() }` before every test, and `MockVaadin.tearDown()` after every test.
* For Spring-based apps it's best to use Spring testing capabilities to bootstrap your app with Spring Test. Then, after that's done, use Spring injector to obtain the instance of your UI:
call `MockVaadin.setup { beanFactory!!.getBean(MainUI::class.java) }` before every test, and `MockVaadin.tearDown()` after every test. Please see the [karibu-testing-spring](https://github.com/mvysny/karibu-testing-spring) example project
for more details.
* For JavaEE-based apps you need to figure out how to launch your app in some kind of embedded JavaEE container in JUnit's JVM.

## More Resources

* The [video](https://www.youtube.com/watch?v=XOhv3y2GXIE) which explains everything behind the browserless testing technique.
* The [browserless web testing](https://mvysny.github.io/browserless-web-testing/) article describes this technique in more depth.
* The [Testing the UI without a browser](https://vaadin.com/blog/testing-the-ui-without-a-browser) blog post summarizes this technique.

This project also offers a mock/fake servlet API implementation, see [mock-servlet-environment](mock-servlet-environment/) for more details.

## FAQ

Q: I'm getting `java.lang.IllegalStateException: UI.getCurrent() must not be null`
   or `no VaadinSession bound to current thread`.

A: You probably forgot to call `MockVaadin.setup()` before the test. Just call `MockVaadin.setup()`
   e.g. from your `@Before`-annotated method if you're using JUnit.
   
Alternatively it could be that Spring is instantiating Vaadin component eagerly
   when the ApplicationContext is constructed. One workaround is to mark
   Vaadin components with `@Lazy` so that they are instantiated lazily.

Q: I'm getting `RouteNotFoundError`/`NotFoundException` instead of my views (Vaadin 14+)

A: The `@Route`-annotated view classes have not been discovered and registered.
   Please discover the routes via `new Routes().autoDiscoverViews("com.example.yourpackage")`
   (make sure to use the correct Java package where all of your views are placed)
   then call `MockVaadin.setup(routes)`.

Also when migrating from Karibu-Testing 1.1 to 1.2.x and you're using a custom `VaadinServlet`,
please make sure to call `routes.register(service.context as VaadinServletContext)` from the
`createServletService()` method, as shown in [Issue #60](https://github.com/mvysny/karibu-testing/issues/60).

Q: Can I integrate CDI (e.g. weld?)

A: You can get inspiration [here](https://github.com/mvysny/karibu-testing/issues/60).

Q: Performance speed-up tips?

1. (Vaadin 14+): the view auto-discovery is rather slow: you can discover the routes
   only once (for example in your `@BeforeClass`-annotated method), then store the `Routes`
   instance into a static field and reuse it for every call to `MockVaadin.setup(routes)`.
2. (Vaadin 14+): `new Routes().autoDiscoverViews("")` is slower than
   `new Routes().autoDiscoverViews("com.example.yourpackage")`
3. (Vaadin 14+): PWA icon computation is horribly slow (2 seconds per test); make
   sure it's off (the `Routes.skipPwaInit` should be `true` which is also the default value).
4. Instead of logging in by filing the login form before every test, you can simply
   login by placing `User` instance into your session directly (this of course
   pretty much depends on how security is handled in your app).
5. The first test is usually slower since all Vaadin-related classes need to be loaded
   (could take 1-2 seconds to run); however any subsequent tests should be much faster
   (~5-60 milliseconds).

# License

Licensed under [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)

Copyright 2017-2018 Martin Vysny

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

# Contributing / Developing

See [Contributing](CONTRIBUTING.md).
