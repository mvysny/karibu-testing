[![Java CI](https://github.com/mvysny/karibu-testing/workflows/Java%20CI/badge.svg)](https://github.com/mvysny/karibu-testing/actions?query=workflow%3A%22Java+CI%22)
[![Join the chat at https://gitter.im/vaadin/vaadin-on-kotlin](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/vaadin/vaadin-on-kotlin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![GitHub tag](https://img.shields.io/github/tag/mvysny/karibu-testing.svg)](https://github.com/mvysny/karibu-testing/tags)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.mvysny.kaributesting/karibu-testing-v8/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.mvysny.kaributesting/karibu-testing-v8)

# Karibu-Testing: The Vaadin Unit Testing

The Unit Testing library for [Vaadin](https://vaadin.com/). Karibu-Testing removes the necessity to run
both the browser and the servlet container in order to test your Vaadin-based apps.

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

## Getting Started / Full Documentation

For the Getting Started documentation and for the full API documentation:

### Vaadin 8

Head to [Testing with Vaadin Framework 8](karibu-testing-v8). Karibu-Testing is compatible with any Vaadin 8.x version.

### Vaadin 14+

Head to [Testing with Vaadin](karibu-testing-v10).
Karibu-Testing is compatible with any Vaadin 14+ version (14, 15, 16, 17 etc).

> *Note:* Starting with version 1.0.0, Karibu-Testing changed the Java package and the Maven group ID in order to be allowed to be present in Maven Central. Be sure
to change the `groupId` to `com.github.mvysny.kaributesting` in your projects.

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

## The Karibu-Testing library is Standalone and Fully Supports Java and Groovy

Even though the Karibu-Testing is endorsed by [Vaadin-on-Kotlin](http://vaadinonkotlin.eu), the Karibu-Testing
does not really depend on any other technology than Vaadin. You don't have to use Vaadin-on-Kotlin nor Karibu-DSL to use Karibu-Testing.
You don't even need to write your app nor your tests in Kotlin, since the library provides native API
for Kotlin, Java and Groovy.
You can thus plug this library in into your Java+Vaadin-based project as a test dependency, and write the test code
in Java, Kotlin or Groovy, whichever suits you more.

Karibu-Testing is published on Maven Central, so it's very easy to add as a Maven dependency.

## Example Projects

A list of a very simple example projects that employ Karibu Testing:

* Vaadin 8 + Kotlin + Gradle + [DynaTest](https://github.com/mvysny/dynatest): [karibu-helloworld-application](https://github.com/mvysny/karibu-helloworld-application)
* Vaadin 8 + Java + Maven + JUnit 5: [vaadin8-sqldataprovider-example](https://github.com/mvysny/vaadin8-sqldataprovider-example)
* Vaadin 14 + Kotlin + Gradle + [DynaTest](https://github.com/mvysny/dynatest): [karibu10-helloworld-application](https://github.com/mvysny/karibu10-helloworld-application)
* Vaadin 14 + Java + Maven + JUnit 5: [vaadin10-sqldataprovider-example](https://github.com/mvysny/vaadin10-sqldataprovider-example)
* Vaadin 8 + Spring Boot + Java + Maven + JUnit 4: [karibu-testing-spring](https://github.com/mvysny/karibu-testing-spring)
* Vaadin 14 + Spring Boot + Java + Maven + JUnit 5: [t-shirt-shop-example](https://github.com/mvysny/t-shirt-shop-example)
* Vaadin 14 + Vaadin-on-Kotlin + Gradle + DynaTest: [Bookstore Example](https://github.com/mvysny/bookstore-vok)
* Vaadin 14 + Vaadin-on-Kotlin + Gradle + DynaTest: [Beverage Buddy VoK](https://github.com/mvysny/beverage-buddy-vok)
* Vaadin 14 + Vaadin-on-Kotlin + Gradle + DynaTest: [Vaadin Kotlin PWA](https://github.com/mvysny/vaadin-kotlin-pwa)

## Integrating Karibu-Testing With Your App

Absolute requirement in order for the Karibu-Testing library to work with your app is:

**You must be able to "start" your app in the same JVM which runs the tests.**

This is because Karibu-Testing uses Vaadin server-side API directly to assert the application state; it uses `UI.getCurrent()` to locate
any components you test on. Your tests therefore need to have access to `UI.getCurrent()` populated by your app.

Here are a few tips for typical apps on how to achieve that:

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

## FAQ

Q: I'm getting `java.lang.IllegalStateException: UI.getCurrent() must not be null`
   or `no VaadinSession bound to current thread`.

A: You probably forgot to call `MockVaadin.setup()` before the test. Just call `MockVaadin.setup()`
   e.g. from your `@Before`-annotated method if you're using JUnit.
   
A: Alternatively it could be that Spring is instantiating Vaadin component eagerly
   when the ApplicationContext is constructed. One workaround is to mark
   Vaadin components with `@Lazy` so that they are instantiated lazily.

Q: I'm getting `RouteNotFoundError` instead of my views (Vaadin 14+)

A: The `@Route`-annotated view classes have not been discovered and registered.
   Please discover the routes via `new Routes().autoDiscoverViews("com.example.yourpackage")`
   (make sure to use the correct Java package where all of your views are placed)
   then call `MockVaadin.setup(routes)`.
   
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
