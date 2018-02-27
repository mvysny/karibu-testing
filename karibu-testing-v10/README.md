# Getting started: Vaadin 10

To start, just add the following lines into your Gradle `build.gradle` file:

```groovy
repositories {
    maven { url "https://dl.bintray.com/mvysny/github" }
}
dependencies {
    testCompile "com.github.kaributesting:karibu-testing-v10:0.4.0"
}
```

You will also need to add the Kotlin language support into your project, to at least compile the testing classes: [Setup Kotlin Using Gradle](https://kotlinlang.org/docs/reference/using-gradle.html).

## Writing your first test

## Writing your first test

Let's start by testing a custom component. Say that we have a `GreetingLabel` which greets the user nicely:
```kotlin
class GreetingLabel : Div() {
    fun greet(name: String) {
        text = "Hello, $name"
    }
}
```

We want to test the component so that a call to the `greet("world")` function will properly set the label's text:

```kotlin
class MyUITest : DynaTest({
    test("proper greeting") {
        val label = GreetingLabel()
        label.greet("world")
        expect("Hello, world") { label.text }
    }
})
```

Nothing special here - we have just instantiated the component as we would a regular Java object, and then we asserted that the text is updated properly.
The test actually works, no further setup is necessary.

You can apply this approach to test even larger components;
since `@Route`-annotated views are components as well, you can test individual views with this approach as well.
However, soon you will hit the limit:

* Your components will probably start to fail at some point if they'll use `UI.getCurrent()` or `VaadinSession.getCurrent()`, since that will
  just return `null`;
* Your views can't perform a navigation because the `RouteRegistry` has not been populated. Vaadin Flow can
  auto-populate route registry only when running inside of a servlet container. 

In order to fix that, we need to mock the Vaadin environment properly.
This is where the `karibu-testing` library comes handy - it provides you with means of mocking the Vaadin environment.

## Testing an actual application

Let's look at the [Vaadin 10 Karibu-DSL Helloworld Application](https://github.com/mvysny/karibu10-helloworld-application) - a very simple application
consisting of just the `MainView` class. Because of its simplicity it is an excellent testing grounds for your experiments -
just clone the app and start experimenting. You can run the tests simply by running `./gradlew`; you can also right-click on the `MainViewTest`
class from your IDE and select 'Run MainViewTest', to run and/or debug the tests from your IDE.

The UI class is simple:

todo

## A more complete example

The [Vaadin 10 Karibu-DSL Helloworld Application](https://github.com/mvysny/karibu10-helloworld-application) is a very simple project consisting of just
one view and a single test for that view.

Please head to the [Beverage Buddy](https://github.com/mvysny/beverage-buddy-vok/) for a more complete example application -
it is a very simple Vaadin-on-Kotlin-based
full-stack Vaadin 10 application which also sports a complete testing suite.
