# Getting started: Vaadin 8

To start, just add the following lines into your Gradle `build.gradle` file:

```groovy
repositories {
    maven { url "https://dl.bintray.com/mvysny/github" }
}
dependencies {
    testCompile "com.github.kaributesting:karibu-testing-v8:0.4.0"
}
```

You will also need to add Kotlin support to your project, even if it will compile the testing classes only: [Using Gradle](https://kotlinlang.org/docs/reference/using-gradle.html).

## Writing your first test

Let's start by testing a custom component. Say that we have a `GreetingLabel` which greets the user nicely:
```kotlin
class GreetingLabel : Label() {
    fun greet(name: String) {
        value = "Hello, $name"
    }
}
```

We want to test that calling `greet("world")` will properly set the label's value:
```kotlin
class MyUITest : DynaTest({
    test("proper greeting") {
        val label = GreetingLabel()
        label.greet("world")
        expect("Hello, world") { label.value }
    }
})
```

Nothing special here - we have just instantiated the label as any other object, and then asserted that the value is updated properly.

With this approach you can test even larger components; since views are components as well you can test individual views with this approach as well.
However, soon you will hit the limit:

* Your components will probably start to fail because `UI.getCurrent()` or `VaadinSession.getCurrent()`
returns `null`;
* Your views can't navigate because the Navigator has not been configured, etc.

We need to mock the Vaadin environment properly.

## Testing an actual application

This is where the `karibu-testing` library comes handy - it provides you with means of mocking the Vaadin environment.

Let's look at the [Karibu-DSL Helloworld Application](https://github.com/mvysny/karibu-helloworld-application) - a very sample application
consisting of just the UI - no views. Because of its simplicity it is an excellent testing grounds for your experiments -
just clone the app and start experimenting. You can run the tests simply by running `./gradlew`; you can also right-click on the `MyUITest`
class from your IDE and select 'Run MyUITest', to run and/or debug all the tests from your IDE.

The UI class is simple:

```kotlin
class MyUI : UI() {

    @Override
    override fun init(vaadinRequest: VaadinRequest?) {
        lateinit var layout: VerticalLayout
        layout = verticalLayout {
            val name = textField {
                caption = "Type your name here:"
            }
            button("Click Me", {
                println("Thanks ${name.value}, it works!")
                layout.label("Thanks ${name.value}, it works!")
            })
        }
    }
}
```

It will produce the following screen:

![Karibu-DSL Helloworld App](../images/karibu_helloworld_app.png)

In order to test this app, we need to initialize `MyUI`. But it's not THAT simple: in order to properly initialize the `UI` class, a proper Vaadin
environment needs to be prepared:

* We need to prepare the `VaadinSession` in a way that `VaadinSession.getCurrent()` returns a proper session
* We need to run the testing code with Vaadin lock obtained (since we're going to invoke Vaadin components and that can only be done on the UI thread)
* We need to create the UI instance and initialize it properly - besides other things we need to call the `UI.init()` method.

Luckily, this is exactly what the `MockVaadin.setup()` function does. It will prepare everything for us and even initialize the `UI`; we just need
to provide the `UI` instance to the function:

```kotlin
class MyUITest : DynaTest({
    beforeEach { MockVaadin.setup({ MyUI() }) }
})
```

> We're using the [DynaTest](https://github.com/mvysny/dynatest) testing framework which runs on top of JUnit5. You can of course use whatever
testing library you wish.

We can verify that everything is prepared, simply by obtaining the current UI contents and asserting that it is a `VerticalLayout` (since our
simple testing app uses `VerticalLayout` as the root component):

```kotlin
class MyUITest : DynaTest({
    beforeEach { MockVaadin.setup({ MyUI() }) }
    test("simple UI test") {
        val layout = UI.getCurrent().content as VerticalLayout
        expect(2) { layout.componentCount }
    }
})
``` 

We can now examine and assert on the layout's properties, and more importantly, discover its children (the `TextField` and `Button`, respectively).
When we obtain the `TextField` instance, we can simply call `setValue("world")` on it, to simulate the user input. Then, we can call `Button.click()`
to simulate a click on the button itself; then we can check that the click listener was run and it had created the label.

Obtaining the `TextField` in this simple project is easy; however typical Vaadin apps has much more complex structure, with lots of nested layouts.
We need some kind of a lookup function which will find the appropriate component for us.

Karibu-Testing library provides three functions for this purpose:

* `_get<Button> { caption = "Click me" }` will find exactly one button with given caption. The function will fail
  if there is no such component, or if there are too many of matching components.
* `_find<VerticalLayout> { styles = "material" }` will find a list of VerticalLayouts containing given style. The function will return
  an empty list if there is no such component.
* `_expectNone<type of component> { criteria }` will expect that there is no component matching given criteria; the function will fail if
  one or more components are matching.

Those functions will search for all components nested within `UI.getCurrent()`. You can also restrict the search to some particular layout
which is handy when you're testing a standalone custom UI component which is not attached to the UI:

* `component._get<type of component> { criteria }` will find exactly one component of given type amongst the `component` and all of its children and descendants.

So, `_get<Button> { caption = 'Click me' }` is merely an shorthand for `UI.getCurrent()._get<Button> { caption = 'Click me' }`

With this arsenal at hand, we can rewrite the test:

```kotlin
class MyUITest : DynaTest({
    beforeEach { MockVaadin.setup({ MyUI() }) }
    test("simple UI test") {
        // simulate a text entry as if entered by the user
        _get<TextField> { caption = "Type your name here:" }.value = "Baron Vladimir Harkonnen"

        // simulate a button click as if clicked by the user
        _get<Button> { caption = "Click Me" }._click()

        // verify that there is a single Label and assert on its value
        expect("Thanks Baron Vladimir Harkonnen, it works!") { _get<Label>().value }
    }
})
``` 

> **Important note:** The lookup methods will only consider *visible* components - for example `_get<Button>()` will fail if the
  "Click me" button is invisible. This is because the intent of the test is to populate/access the components as if it was the user who
  is accessing the application; and of course the user can't access the component if it is invisible.

## Example project

The [Karibu-DSL Helloworld Application](https://github.com/mvysny/karibu-helloworld-application) is a very simple project consisting of just
one view and a single test for that view.

## Advanced topics

### Navigation

A typical app will consist of multiple views. You can test those views using two different approaches:

* Simply instantiate the view class yourself and test it as a component, as demostrated above with `GreetingLabel`. It typically extends `VerticalLayout` or some other layout anyway,
  which makes it a Vaadin component. The disadvantage is that `_get()` functions will not work unless you attach the component to the current UI;
  also the component may lazy-initialize itself by the means of the onAttach listener which only gets fired when the component is attached to a UI.
  Therefore, this approach should only be used for reusable components which do not depend on a particular UI.
* Make `MockVaadin.setup()` instantiate your UI, which should in turn initialize the Navigator anyway. Now you can simply use the Navigator's API
  to perform the navigation to the view: `UI.getCurrent().navigator.navigateTo("books")`. However, if your UI does not configure the Navigator itself,
  this won't work unless the test itself will populate the Navigator.

If you are using Karibu-DSL or Vaadin-on-Kotlin (which uses Karibu-DSL under the hood), populating the Navigator is quite simple. Just check out the
[Vaadin-on-Kotlin Example app](https://github.com/mvysny/vaadin-on-kotlin#example-project) sources. In short:

* Karibu-DSL provides the `@AutoView` annotation with which you should annotate your views
* Then it contains the `AutoViewProvider` class which will use Servlet container to find all classes annotated with the `@AutoView` annotation.
* Karibu-DSL also provides the `autoViewProvider` which will then resolve those views for Vaadin `Navigator`.
* All you need to do in your UI is to set the view provider to the navigator, as follows:

```kotlin
navigator = Navigator(this, content as ViewDisplay)
navigator.addProvider(autoViewProvider)
```

With serverless tests there is no servlet container and nobody will discover the `@AutoView`s automatically.
Luckily Karibu-DSL provides means to discover those views, as a `autoDiscoverViews()` function. All you need to do in your tests is
to call this function before all tests:

```kotlin
class MyUITest : DynaTest({
    beforeGroup {
        autoDiscoverViews("com.myproject")
    }
    beforeEach { MockVaadin.setup({ MyUI() }) }
    test("simple test") {
        navigateToView<CrudView>()  // this will call Navigator.navigateTo("crud")

        // now the view is ready and attached to your UI. We can test.
        val grid = _get<Grid<*>>()
        // etc etc
    }
}
```

