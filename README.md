[![Build Status](https://travis-ci.org/mvysny/karibu-testing.svg?branch=master)](https://travis-ci.org/mvysny/karibu-testing)
[![Join the chat at https://gitter.im/vaadin/vaadin-on-kotlin](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/vaadin/vaadin-on-kotlin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![GitHub tag](https://img.shields.io/github/tag/mvysny/karibu-testing.svg)](https://github.com/mvysny/karibu-testing/tags)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.mvysny.kaributesting/karibu-testing-v8/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.mvysny.kaributesting/karibu-testing-v8)

# Vaadin Browserless Testing

[Vaadin-on-Kotlin](http://vaadinonkotlin.eu) promotes a testing technique called *browserless testing*. With this approach, it's not the browser you issue
testing instructions to: we bypass the browser and the JavaScript-Server bridge, and talk directly to the server Vaadin component API.

This approach has the following advantages:

* *Fast*: Browserless tests are typically 100x faster than Selenium-based tests and run in 5-60 milliseconds,
  depending on their complexity.
* *Reliable*: We don't need arbitrary sleeps since we're server-side and we can await until the request is fully processed.
  We don't use Selenium drivers which are known to fail randomly.
* *Headless*: The tests run headless since there's no browser.

With this technique you can run 600 UI tests in 7 seconds, as opposing to 1-2 hours with Selenium-based approach.
Because of the speed, you can let the UI tests run after every commit by your continuous integration server.

Since we're bypassing the browser and talking to Vaadin server API directly, you don't even need to start the servlet container -
you can just add the server jars onto testing classpath and call Vaadin server API which will in turn invoke your server logic.

A 15-minute [video](https://www.youtube.com/watch?v=XOhv3y2GXIE) explains everything behind the browserless testing technique.

## The Karibu-Testing library is Standalone and Fully Supports Java

Even though the Karibu-Testing is endorsed by [Vaadin-on-Kotlin](http://vaadinonkotlin.eu), the Karibu-Testing
does not really depend on any other technology than Vaadin. You don't have to use Vaadin-on-Kotlin nor Karibu-DSL to use Karibu-Testing.
You don't even need to write your app nor your tests in Kotlin, since the library provides both Kotlin and Java API.
You can thus plug this library in into your Java+Vaadin-based project as a test dependency, and write the test code
in Java or Kotlin, whichever suits you more.

Karibu-Testing is published on Maven Central, so it's very easy to add as a Maven dependency.

## Full Documentation

For the Getting Started documentation and for the full API documentation:

* If you are using Vaadin 8, head to [Testing with Vaadin 8](karibu-testing-v8). Karibu-Testing is compatible with any Vaadin 8.x version.
* If you are using Vaadin 10, head to [Testing with Vaadin 10](karibu-testing-v10). Karibu-Testing is compatible with any Vaadin 1x version (10, 11, etc).

## Example Projects

A list of a very simple example projects that employ Karibu Testing:

* Vaadin 8 + Kotlin + Gradle + [DynaTest](https://github.com/mvysny/dynatest): [karibu-helloworld-application](https://github.com/mvysny/karibu-helloworld-application)
* Vaadin 8 + Java + Maven + JUnit 5: [vaadin8-sqldataprovider-example](https://github.com/mvysny/vaadin8-sqldataprovider-example)
* Vaadin 10 + Kotlin + Gradle + [DynaTest](https://github.com/mvysny/dynatest): [karibu10-helloworld-application](https://github.com/mvysny/karibu10-helloworld-application)
* Vaadin 10 + Java + Maven + JUnit 5: [vaadin10-sqldataprovider-example](https://github.com/mvysny/vaadin10-sqldataprovider-example)
* Vaadin 8 + Spring Boot + Java + Maven + JUnit 4: [karibu-testing-spring](https://github.com/mvysny/karibu-testing-spring)
* Vaadin 10 + Spring Boot + Java + Maven + JUnit 4: [t-shirt-shop-example](https://github.com/mvysny/t-shirt-shop-example)
* Vaadin 10 + Vaadin-on-Kotlin + Gradle: [Bookstore Example](https://github.com/mvysny/bookstore-vok)
* Vaadin 10 + Vaadin-on-Kotlin + Gradle: [Beverage Buddy VoK](https://github.com/mvysny/beverage-buddy-vok)
* Vaadin 10 + Vaadin-on-Kotlin + Gradle: [Vaadin Kotlin PWA](https://github.com/mvysny/vaadin-kotlin-pwa)

## Integrating Karibu-Testing With Your App

Absolute requirement in order for the Karibu-Testing library to work with your app is:

* You must be able to "start" your app in the same JVM which runs the tests.

Here are a few tips for typical apps:

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
* The [browserless web testing](http://mavi.logdown.com/posts/3147601) article describes this technique in more depth.

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
