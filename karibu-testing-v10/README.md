[![GitHub tag](https://img.shields.io/github/tag/mvysny/karibu-testing.svg)](https://github.com/mvysny/karibu-testing/tags)

# Getting started: Vaadin 10

To start, just add the following lines into your Gradle `build.gradle` file:

```groovy
repositories {
    maven { url "https://dl.bintray.com/mvysny/github" }
}
dependencies {
    testCompile "com.github.kaributesting:karibu-testing-v10:x.y.z"
}
```

> Note: obtain the newest version from the tag name above

You will also need to add the Kotlin language support into your project, to at least compile the testing classes: [Setup Kotlin Using Gradle](https://kotlinlang.org/docs/reference/using-gradle.html).

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

The `MainView` class is simple:

```kotlin
class MainView : VerticalLayout() {
    private lateinit var template: ExampleTemplate
    init {
        button("Click me") {
            onLeftClick {
                template.value = "Clicked!"
            }
        }
        template = exampleTemplate()
    }
}
```

It will produce the following screen:

![Vaadin 10 Karibu-DSL Helloworld App](../docs/images/karibu10_helloworld_app.png)

> **Note:** I've used the [Karibu-DSL](https://github.com/mvysny/karibu-dsl) library to define the UI, however
  you are free to create your UI in any way you see fit: be it design html files, or even plain Java code.
  All of those styles are compatible with the Karibu-Testing library - it doesn't matter how exactly the UI has been created.

In order to test this app, we need to instantiate and initialize an `UI`. In order to properly initialize the `UI` class, a proper Vaadin
environment needs to be prepared:

* We need to prepare the `VaadinSession` in a way that `VaadinSession.getCurrent()` returns a proper session
* We need to run the testing code with Vaadin lock obtained (since we're going to invoke Vaadin components and that can only be done on the UI thread)
* We need to create the UI instance and initialize it properly - besides other things we need to call the `UI.doInit()` method.

Luckily, this is exactly what the `MockVaadin.setup()` function does. It will prepare everything for us and even initialize the `UI`; we just need
to provide the auto-detected set of `@Route`s to the function:

```kotlin
class MyUITest : DynaTest({
    beforeEach { MockVaadin.setup(Routes().autoDiscoverViews("com.vaadin.flow.demo")) }
})
```

> **Tip:** We're using the [DynaTest](https://github.com/mvysny/dynatest) testing framework which runs on top of JUnit5. You can of course use whatever
testing library you prefer.

We can verify that everything is prepared correctly, simply by obtaining the current UI contents and asserting that it is a `MainView` (since our
simple testing app uses `MainView` as the root route):

```kotlin
class MyUITest : DynaTest({
    beforeEach { MockVaadin.setup({ MyUI() }) }
    test("simple UI test") {
        val main = UI.getCurrent().children.findFirst().get() as MainView
        expect(2) { main.children.count() }
    }
})
``` 

### Simulating the user input

We can now examine and assert on the MainView's properties, and more importantly,
discover its children (the `ExampleTemplate` and `Button`, respectively).
When we obtain the `Button` instance, we can simply call the server-side `click()` API on it, to simulate a click on the button itself.
The `click()` method will execute all listeners and will block until
all listeners are done; we can check that the click listener was run and it had changed the value
of the template, by examining the value of `ExampleTemplate`.

> Note: as you'll learn later on, neither `Button.click()` nor `setValue()` will check for whether the component is enabled or not.
Therefore it's important to use `_click()` and `_value` instead.

Obtaining the `Button` in this simple project is easy - it's the first child of the `MainView`
so we can simply call `children.findFirst().get() as Button` to obtain the button.
However, typical Vaadin apps has much more complex structure with lots of nested layouts.
We need some kind of a lookup function which will find the appropriate component for us.

### Looking up the components

The Karibu-Testing library provides three functions for this purpose; for now we are only interested in one of them:

* `_get<type of component> { criteria }` will find exactly one component of given type, matching given criteria, in the current UI. The function will fail
  if there is no such component, or if there are too many of matching components. For example: `_get<Button> { caption = "Click me" }`

This particular function will search for all components nested within `UI.getCurrent()`.
You can call the function in a different way, which will restrict the search to some particular layout
which is handy when you're testing a standalone custom UI component outside of the `UI` class:

* `component._get<type of component> { criteria }` will find exactly one component of given type amongst the `component` and all of its children and descendants.

> **Info:** `_get<Button> { caption = 'Click me' }` is merely an shorthand for `UI.getCurrent()._get<Button> { caption = 'Click me' }`.

With this arsenal at hand, we can rewrite the test:

```kotlin
class MainViewTest: DynaTest({
    beforeEach { MockVaadin.setup(Routes().autoDiscoverViews("com.vaadin.flow.demo")) }

    test("test greeting") {
        // simulate a button click as if clicked by the user
        _get<Button> { caption = "Click me" } ._click()

        // look up the Example Template and assert on its value
        expect("Clicked!") { _get<ExampleTemplate>().value }
    }
})
``` 

> **Important note:** The lookup methods will only consider *visible* components - for example `_get<Button>()` will fail if the
  "Click me" button is invisible. This is because the intent of the test is to populate/access the components as if it was the user who
  is accessing the application; and of course the user can't access the component if it is invisible.

## Example projects

The [Vaadin 10 Karibu-DSL Helloworld Application](https://github.com/mvysny/karibu10-helloworld-application) is a very simple project consisting of just
one view and a single test for that view.

Please head to the [Beverage Buddy](https://github.com/mvysny/beverage-buddy-vok/) for a more complete example application -
it is a very simple Vaadin-on-Kotlin-based
full-stack Vaadin 10 application which also sports a complete testing suite.

## Advanced topics

### Navigation

A typical app will consist of multiple views. You can test the views of such app using two different approaches:

* Simply instantiate the view class yourself and test it as a component, as demonstrated above with `GreetingLabel`.
  The view typically extends `VerticalLayout` or some other layout anyway,
  which makes it a Vaadin component. The disadvantage is that `_get()` functions will not work unless you attach the component to the current UI;
  also the component may lazy-initialize itself by the means of the `onAttach()` listener which only gets fired when the component is attached to a UI.
  Therefore, this approach should only be used for reusable components which do not depend on a particular UI and do not
  lazy-init themselves.
* Properly set up your UI by calling `MockVaadin.setup(Routes().autoDiscoverViews("your.app.package"))`. The `autoDiscoverViews()` function will
  automatically discover all of your `@Route`-annotated views; `MockVaadin` will then properly populate the internal Vaadin Flow `RouteRegistry`.
  Because of that, you can simply call the navigation from your tests to perform the navigation to the view, for example
  `UI.getCurrent().navigateTo("books")`.

Typically Flow will rely on Servlet container to auto-discover all routes. However, with browserless tests there is no servlet container and
nobody will discover the `@Route`s automatically. That's why Karibu-Testing library provides means to discover those views, in the form of
the `autoDiscoverViews()` function. All you need to do in your tests is
to call this function before all tests:

```kotlin
class MyUITest : DynaTest({
    beforeEach { MockVaadin.setup(Routes().autoDiscoverViews("com.vaadin.flow.demo")) }
    test("simple test") {
        // navigate to the "Categories" list route.
        UI.getCurrent().navigateTo("categories")

        // now the "Categories" list should be attached to your UI and displayed. Look up the Grid and assert on its contents.
        val grid = _get<Grid<*>>()
        expect(1) { grid.dataProvider._size() }
        // etc etc
    }
})
```

### Polymer Templates

Testing components nested inside Polymer Templates with browserless approach is nearly impossible since the child components are either
not accessible from the server-side altogether, or they are only a shallow version of components constructed server-side.

Please read the discussion at [Can't look up components inside of PolymerTemplate](https://github.com/mvysny/karibu-testing/issues/1)
for a technical explanation.

In short, it is not possible to look up components inside of a Polymer Template, but it is possible to use the API of nested components
to a degree. It is recommended that you publish your `@Id`-annotated fields as `public` or `internal` and access them from your tests
in the following manner:

```kotlin
class ReviewsList : PolymerTemplate<TemplateModel>() {
    @Id("search")
    internal lateinit var search: TextField
    // ...
}

test("create review") {
   // this doesn't work because of https://github.com/mvysny/karibu-testing/issues/1
   //  _get<Button> { caption = "New review" } ._click()
   
   // this will work:
   _get<ReviewsList>().addReview._click()
}
```

## API

### Looking up components

This library provides three methods for looking up components.

* `_get<type of component> { criteria }` will find exactly one **visible** component of given type in the current UI, matching given criteria. The function will fail
  if there is no such component, or if there are too many of matching **visible** components. For example: `_get<Button> { caption = "Click me" }`
* `_find<type of component> { criteria }` will find a list of matching **visible** components of given type in the current UI. The function will return
  an empty list if there is no such component. For example: `_find<VerticalLayout> { id = "form" }`
* `_expectNone<type of component> { criteria }` will expect that there is no **visible** component matching given criteria in the current UI; the function will fail if
  one or more components are matching. For example: `_expectNone<Button> { caption = "Delete" }`

> I can't stress the **visible** part enough. Often the dump will show the button, the caption will be correct and everything
  will look OK but the lookup method will claim the component is not there. The lookup methods only search for visible
  components - they will simply ignore invisible ones.

This set of functions operates on `UI.getCurrent()`. However, often it is handy to test a component separately from the UI, and perform the lookup only
in that component. There are `Component._get()`, `Component._find()` and `Component._expectNone()` counterparts, added to every Vaadin
component as an extension method. For example:

```kotlin
class AddNewPersonForm : VerticalLayout {
    // nests fields, uses binder, etc etc
}

test("add new person happy flow") {
    val form = AddNewPersonForm()
    form._get<TextField> { caption = "Name:" } ._value = "John Doe"
    form._get<Button> { caption = "Create" } ._click()
}
```

Such methods are also useful for example when locking the lookup scope into a particular container, say, some particular layout:
```kotlin
_get<FlexLayout> { id = "form" } ._get<TextField> { caption = "Age" } ._value = "45"
```

Since there is no way to see the UI of the app with this kind of approach (since there's no browser), the lookup functions will dump the component tree
on failure. For example if I make a mistake in the lookup caption, the `_get()` function will fail:
```
java.lang.IllegalArgumentException: No visible TextField in MyUI[] matching TextField and caption='Type your name': []. Component tree:
└── MyUI[]
    └── VerticalLayout[]
        ├── TextField[caption='Type your name here:', value='']
        └── Button[caption='Click Me']


	at com.github.karibu.testing.LocatorKt._find(Locator.kt:102)
	at com.github.karibu.testing.LocatorKt._get(Locator.kt:65)
	at com.github.karibu.testing.LocatorKt._get(Locator.kt:86)
	at org.test.MyUITest$1$2.invoke(MyUITest.kt:25)
	at org.test.MyUITest$1$2.invoke(MyUITest.kt:12)
```

### Clicking Buttons

Vaadin Button contains the `click()` method, however that method actually invokes the browser-side click method which will then eventually
fire server-side click listeners. However, with browserless testing there is no browser and nothing gets done.

It is therefore important that we use the `Button._click()` extension method provided by the Karibu Testing library, which moreover
checks the following points prior running the click listeners:

* When writing the test,
  we expect the button to be enabled and fully able to receive (and execute) clicks. In this case, an attempt to click such button
  from a test will fail.
* If the button is effectively invisible (it may be visible itself, but it's nested in a layout that's invisible), the user can't really
  interact with the button. In this case, the `_click()` method will fail as well.

### Changing values

The `HasValue.setValue()` function succeeds even if the component in question is disabled or read-only. However, when we
want to simulate user input and we want to change the value of, say, a `Combobox`, we expect the Combobox to be enabled,
read-write, visible; in other words, fully prepared to receive user input.

It is therefore important to use the `HasValue._value` extension property provided by the Karibu Testing library, which checks
all the above items prior setting the new value.

### Support for Grid

The Vaadin Grid is the most complex component in Vaadin, and therefore it requires a special set of testing methods, to assert the state and
contents of the Grid.

* You can retrieve a bean at particular index; for example `grid._get(0)` will return the first item.
* You can check for the total amount of items shown in the grid, by calling `grid._size()`
* You can obtain a full formatted row as seen by the user, by calling `grid._getFormattedRow(rowIndex)` - it will return that particular row as
  `List<String>`
* You can assert on the number of rows in a grid, by calling `grid.expectRows(25)`. If there is a different amount of rows, the function will
  fail and will dump first 10 rows of the grid, so that you can see the actual contents of the grid.
* You can assert on a formatted output of particular row of a grid: `grid.expectRow(rowIndex, "John Doe", "25")`. If the row looks different,
  the function will fail with a proper grid dump.

## Adding support for custom search criteria

Suppose you have a dynamic form which allows you to add multiple addresses, one of those being primary. You'll have the following `AddressPanel` class:

```kotlin
class AddressPanel : FormLayout() {
    init {
        checkBox("Primary Address")
        textField("Street")
    }
}
```

The class seems to be lacking a property telling whether the address is primary or not. Luckily, we can fix that with the help of extension properties:

```kotlin
val AddressPanel.isPrimary: Boolean get() = _get<CheckBox> { caption = "Primary Address" } ._value
```

Now suppose we want to find the primary address, failing if there is none. The `_get()` function uses the `SearchSpec<T>` to filter
the components. The `SearchSpec<T>` has one extension point: the `predicates` property which allow you to add any additional conditions on the
components considered by the function. We can indeed add our own predicate that takes advantage of the `isPrimary` property.
The primary address lookup code will look like this:

```kotlin
test("check that the form has primary address") {
    UI.getCurrent().content = AddressPanel()
    _get<AddressPanel> { predicates.add(Predicate { address -> address.isPrimary }) }
}
```

This code has two disadvantages:

* It is ugly and hard to read
* When no such addresses are found, a cryptic error message is printed: `No visible AddressPanel in MockUI[] matching AddressPanel and com.github.karibu.testing.LocatorTest$1$6$1$1@32eebfca: []`

We can do better. We can extract the above code to a property; since we need access to `predicates` we need the property to be an extension
property of `SearchSpec<T>`. We also need to provide a proper `toString()` to produce clear error messages.

```kotlin
var SearchSpec<AddressPanel>.isPrimary: Boolean
    @Deprecated("", level = DeprecationLevel.ERROR)
    get() = throw UnsupportedOperationException()
    set(value) {
        predicates.add(object : Predicate<AddressPanel> {
            override fun test(t: AddressPanel) = t.isPrimary == value
            override fun toString() = "isPrimary==$value"
        })
    }
```

Now we can simply look up the primary address as follows:

```kotlin
test("check that the form has primary address") {
    UI.getCurrent().content = AddressPanel()
    _get<AddressPanel> { isPrimary = true }
}
```

The error message of a failed test is now clear:

```
java.lang.IllegalArgumentException: No visible AddressPanel in MockUI[] matching AddressPanel and isPrimary==true: []. Component tree:
└── MockUI[]
    └── AddressPanel[]
        ├── CheckBox[caption='Primary Address', value='false']
        └── TextField[caption='Street', value='']
```

## Using Karibu-Testing with Spring or Guice

To have dependencies injected into your views as they are constructed, you need to register a custom `Instantiator` into Vaadin.
The [Vaadin Spring Add-on](https://vaadin.com/directory/component/vaadin-spring) provides an implementation of the `Instantiator` which you can use.

To use a custom `Instantiator`:
 
1. Implement your own `VaadinServletService` (or extend pre-provided `MockService`) and override `VaadinServletService.loadInstantiators`,
   to load a proper Instantiator
2. Register your `VaadinServletService` in `MockVaadin.setup()`, the `serviceFactory` parameter.
