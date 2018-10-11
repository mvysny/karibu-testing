[![GitHub tag](https://img.shields.io/github/tag/mvysny/karibu-testing.svg)](https://github.com/mvysny/karibu-testing/tags)

# Getting started: Vaadin 8

To start, just add the following lines into your Gradle `build.gradle` file:

```groovy
repositories {
    maven { url "https://dl.bintray.com/mvysny/github" }
}
dependencies {
    testCompile "com.github.kaributesting:karibu-testing-v8:x.y.z"
}
```

> Note: obtain the newest version from the tag name above

You will also need to add the Kotlin language support into your project, to at least compile the testing classes: [Setup Kotlin Using Gradle](https://kotlinlang.org/docs/reference/using-gradle.html).

## Writing your first test

Let's start by testing a custom component. Say that we have a `GreetingLabel` which greets the user nicely:
```kotlin
class GreetingLabel : Label() {
    fun greet(name: String) {
        value = "Hello, $name"
    }
}
```

We want to test the component so that a call to the `greet("world")` function will properly set the label's value:

Kotlin:
```kotlin
class MyUITest : DynaTest({
    test("proper greeting") {
        val label = GreetingLabel()
        label.greet("world")
        expect("Hello, world") { label.value }
    }
})
```

Java:
```java
public class MyUITest {
    @Test
    public void testProperGreeting() {
        final GreetingLabel label = new GreetingLabel();
        label.greet("world");
        assertEquals("Hello, world", label.getValue());
    }
}
```

Nothing special here - we have just instantiated the component as we would a regular Java object, and then we asserted that the value is updated properly.
The test actually works, no further setup is necessary.

You can apply this approach to test even larger components;
since views are components as well, you can test individual views with this approach as well.
However, soon you will hit the limit:

* Your components will probably start to fail at some point if they'll use `UI.getCurrent()` or `VaadinSession.getCurrent()`, since that will
  just return `null`;
* Your views can't perform a navigation because the `Navigator` has not been configured.

In order to fix that, we need to mock the Vaadin environment properly.
This is where the `karibu-testing` library comes handy - it provides you with means of mocking the Vaadin environment.

## Testing an actual application

Let's look at the [Karibu-DSL Helloworld Application](https://github.com/mvysny/karibu-helloworld-application) - a very simple application
consisting of just the `UI` class and no views. Because of its simplicity it is an excellent testing grounds for your experiments -
just clone the app and start experimenting. You can run the tests simply by running `./gradlew`; you can also right-click on the `MyUITest`
class from your IDE and select 'Run MyUITest', to run and/or debug the tests from your IDE.

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

![Karibu-DSL Helloworld App](../docs/images/karibu_helloworld_app.png)

> **Note:** I've used the [Karibu-DSL](https://github.com/mvysny/karibu-dsl) library to define the UI, however
  you are free to create your UI in any way you see fit: be it design html files, or even plain Java code.
  All of those styles are compatible with the Karibu-Testing library - it doesn't matter how exactly the UI has been created.

In order to test this app, we need to instantiate and initialize `MyUI`. In order to properly initialize the `UI` class, a proper Vaadin
environment needs to be prepared:

* We need to prepare the `VaadinSession` in a way that `VaadinSession.getCurrent()` returns a proper session
* We need to run the testing code with Vaadin lock obtained (since we're going to invoke Vaadin components and that can only be done on the UI thread)
* We need to create the UI instance and initialize it properly - besides other things we need to call the `UI.init()` method.

Luckily, this is exactly what the `MockVaadin.setup()` function does. It will prepare everything for us and even initialize the `UI`; we just need
to provide the `UI` instance to the function:

Kotlin:
```kotlin
class MyUITest : DynaTest({
    beforeEach { MockVaadin.setup({ MyUI() }) }
})
```
Java:
```java
public class MyUITest {
    @BeforeEach
    public void beforeEach() {
        MockVaadin.setup(MyUI::new);
    }
}
```

> **Tip for Kotlin users:** We're using the [DynaTest](https://github.com/mvysny/dynatest) testing framework which runs on top of JUnit5. You can of course use whatever
testing library you prefer.

We can verify that everything is prepared correctly, simply by obtaining the current UI contents and asserting that it is a `VerticalLayout` (since our
simple testing app uses `VerticalLayout` as the root component):

Kotlin:
```kotlin
class MyUITest : DynaTest({
    beforeEach { MockVaadin.setup({ MyUI() }) }
    test("simple UI test") {
        val layout = UI.getCurrent().content as VerticalLayout
        expect(2) { layout.componentCount }
    }
})
```
Java:
```java
public class MyUITest {
    @BeforeEach
    public void beforeEach() {
        MockVaadin.setup(MyUI::new);
    }
    @Test
    public void simpleUITest() {
        final VerticalLayout layout = (VerticalLayout) UI.getCurrent().getContent();
        assertEquals(2, layout.getComponentCount());
    }
}
```

### Simulating the user input

We can now examine and assert on the layout's properties, and more importantly, discover its children (the `TextField` and `Button`, respectively).
When we obtain the `TextField` instance, we can simply call the server-side `setValue("world")` API on it, to simulate the user input.
Then, we can call `Button.click()`
to simulate a click on the button itself. The `click()` method will execute all listeners and will block until
all listeners are done; we can check that the click listener was run and it had created the label.

> Note: as you'll learn later on, neither `Button.click()` nor `HasValue.setValue()` will check for whether the component is enabled or not.
Therefore it's important to use `button._click()` and `nameField._value` instead (for Java users, it's `_click(button)` and `_setValue(nameField, "John")`).

> Note for Java users: don't forget to static-import all static methods from the `LocatorJ` class, in order to use them more easily:
`import static com.github.karibu.testing.LocatorJ.*;`

Obtaining the `TextField` in this simple project is easy - it's the first child of the layout so we can call `getComponent(0) as TextField` to obtain the text field.
However, typical Vaadin apps has much more complex structure with lots of nested layouts.
We need some kind of a lookup function which will find the appropriate component for us.

### Looking up the components

The Karibu-Testing library provides three functions for this purpose; for now we are only interested in one of them:

* `_get<type of component> { criteria }` will find exactly one component of given type, matching given criteria, in the current UI. The function will fail
  if there is no such component, or if there are too many of matching components. For example: Kotlin: `_get<Button> { caption = "Click me" }`.
  Java: `_get(Button.class, spec -> spec.withCaption("Click me"))`

This particular function will search for all components nested within `UI.getCurrent()`. You can call the function in a different way, which will restrict the search to some particular layout
which is handy when you're testing a standalone custom UI component outside of the `UI` class:

* `component._get<type of component> { criteria }` will find exactly one component of given type amongst the `component` and all of its children and descendants.
  in Java: `_get(layout, Button.class, spec -> spec.withCaption("Click me"))`

> **Info:** `_get<Button> { caption = 'Click me' }` is merely an shorthand for `UI.getCurrent()._get<Button> { caption = 'Click me' }`.
  Java: `_get(Button.class, spec -> spec.withCaption("Click me"))` is a shorthand for `_get(UI.getCurrent(), Button.class, spec -> spec.withCaption("Click me"))`.

With this arsenal at hand, we can rewrite the test:

Kotlin:
```kotlin
class MyUITest : DynaTest({
    beforeEach { MockVaadin.setup({ MyUI() }) }
    test("simple UI test") {
        // simulate a text entry as if entered by the user
        _get<TextField> { caption = "Type your name here:" }._value = "Baron Vladimir Harkonnen"

        // simulate a button click as if clicked by the user
        _get<Button> { caption = "Click Me" }._click()

        // verify that there is a single Label and assert on its value
        expect("Thanks Baron Vladimir Harkonnen, it works!") { _get<Label>().value }
    }
})
```
Java:
```java
import static com.github.karibu.testing.LocatorJ.*;
public class MyUITest {
    @BeforeEach
    public void beforeEach() {
        MockVaadin.setup(MyUI::new);
    }

    @Test
    public void simpleUITest() {
        // simulate a text entry as if entered by the user
        _setValue(_get(TextField.class, spec -> spec.withCaption("Type your name here:")), "Baron Vladimir Harkonnen");

        // simulate a button click as if clicked by the user
        _click(_get(Button.class, spec -> spec.withCaption("Click Me"));

        // verify that there is a single Label and assert on its value
        assertEquals("Thanks Baron Vladimir Harkonnen, it works!", _get(Label.class).getValue());
    }
}
```

> **Important note:** The lookup methods will only consider *visible* components - for example `_get<Button>()` will fail if the
  "Click me" button is invisible. This is because the intent of the test is to populate/access the components as if it was the user who
  is accessing the application; and of course the user can't access the component if it is invisible.

## Example projects

The [Karibu-DSL Helloworld Application](https://github.com/mvysny/karibu-helloworld-application) is a very simple project consisting of just
one `UI` and a single test for that UI. Because of its simplicity it's easy to experiment upon.

The [Vaadin-on-Kotlin CRUD Exaple](https://github.com/mvysny/vaadin-on-kotlin#example-project) is a
more complete full-stack app which demonstrates how to use the Navigator and the Views using browserless testing.

## Advanced topics

### Navigation

A typical app will consist of multiple views. You can test the views of such app using two different approaches:

* Simply instantiate the view class yourself and test it as a component, as demonstrated above with `GreetingLabel`.
  The view typically extends `VerticalLayout` or some other layout anyway,
  which makes it a Vaadin component. The disadvantage is that `_get()` functions will not work unless you attach the component to the current UI;
  also the component may lazy-initialize itself by the means of the onAttach listener which only gets fired when the component is attached to a UI.
  Therefore, this approach should only be used for reusable components which do not depend on a particular UI and do not
  lazy-init themselves.
* Properly set up your UI by calling `MockVaadin.setup({ MyUI() })`. Your UI then typically initializes the Navigator and outfits it with a `ViewProvider`.
  Because of that, you can simply call the Navigator's API from your tests to perform the navigation to the view, for example `UI.getCurrent().navigator.navigateTo("books")`.
  However, if the `ViewProvider` was not properly initialized for Navigator itself,
  this won't work unless the test code itself will populate the `ViewProvider`.

If you are using Karibu-DSL or Vaadin-on-Kotlin (which uses Karibu-DSL under the hood), populating the `ViewProvider` is quite simple. Just check out the
test classes in the
[Vaadin-on-Kotlin Example app](https://github.com/mvysny/vaadin-on-kotlin#example-project) on how that's done. In short:

* Karibu-DSL provides the `@AutoView` annotation which you should annotate your views with
* Karibu-DSL also provides the `AutoViewProvider` class which will use Servlet container to find all classes annotated with the `@AutoView` annotation.
* Karibu-DSL also provides the singleton `AutoViewProvider` value via the `autoViewProvider` global property,
  which you register to the Navigator in your UI. That will make Navigator able to resolve all autoviews.
* Therefore, all you need to do in your UI is to set the view provider to the navigator, as follows:

```kotlin
navigator = Navigator(this, content as ViewDisplay)
navigator.addProvider(autoViewProvider)
```

With browserless tests there is no servlet container and nobody will discover the `@AutoView`s automatically.
Luckily Karibu-DSL provides means to discover those views, as a `autoDiscoverViews()` function. All you need to do in your tests is
to call this function before all tests:

Kotlin:
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
})
```

Java:
```java
import static com.github.karibu.testing.LocatorJ.*;
import static com.github.vok.karibudsl.NavigatorKt.*;
public class MyUITest {
    @BeforeAll
    public static void beforeAll() {
        autoDiscoverViews("com.myproject")
    }

    @BeforeEach
    public void beforeEach() {
        MockVaadin.setup(MyUI::new);
    }

    @Test
    public void simpleTest() {
        navigateToView(CrudView.class);  // this will call Navigator.navigateTo("crud")

        // now the view is ready and attached to your UI. We can test.
        final Grid<Person> grid = _get(Grid.class);
        // etc etc
    }
}
```

## API

### Looking up components

This library provides three methods for looking up components.

* `_get<type of component> { criteria }` will find exactly one **visible** component of given type in the current UI, matching given criteria. The function will fail
  if there is no such component, or if there are too many of matching **visible** components. For example: `_get<Button> { caption = "Click me" }`;
  Java: `_get(Button.class, spec -> spec.withCaption("Click me"));`.
* `_find<type of component> { criteria }` will find a list of matching **visible** components of given type in the current UI. The function will return
  an empty list if there is no such component. For example: `_find<VerticalLayout> { styles = "material" }`. Java:
  `_find(Button.class, spec -> spec.withStyles("material"));`
* `_expectNone<type of component> { criteria }` will expect that there is no **visible** component matching given criteria in the current UI; the function will fail if
  one or more components are matching. For example: `_expectNone<Button> { caption = "Delete" }`. Java:
  `_assertNone(Button.class, spec -> spec.withCaption("Delete"));`

> I can't stress the **visible** part enough. Often the dump will show the button, the caption will be correct and everything
  will look OK but the lookup method will claim the component is not there. The lookup methods only search for visible
  components - they will simply ignore invisible ones.

This set of functions operates on `UI.getCurrent()`. However, often it is handy to test a component separately from the UI, and perform the lookup only
in that component. There are `Component._get()`, `Component._find()` and `Component._expectNone()` counterparts, added to every Vaadin
component as an extension method. For example:

Kotlin:
```kotlin
class AddNewPersonForm : VerticalLayout() {
    // nests fields, uses binder, etc etc
}

test("add new person happy flow") {
    val form = AddNewPersonForm()
    form._get<TextField> { caption = "Name:" } ._value = "John Doe"
    form._get<Button> { caption = "Create" } ._click()
}
```

Java:
```java
public class AddNewPersonForm extends VerticalLayout {
    // nests fields, uses binder, etc etc
}

@Test
public void addNewPersonHappyFlow() {
    final AddNewPersonForm form = new AddNewPersonForm();
    _setValue(_get(form, TextField.class, spec -> spec.withCaption("Name:")), "John Doe");
    _click(_get(form, Button.class, spec -> spec.withCaption("Create")));
}
```

Such methods are also useful for example when locking the lookup scope into a particular container, say, some particular tab of a tab sheet:
Kotlin:
```kotlin
_get<TabSheet>().getTab[0].component._get<TextField> { caption = "Age" } ._value = "45"
```
Java:
```java
final TabSheet ts = _get(TabSheet.class);
final Component first = ts.getTab(0).getComponent();
_setValue(_get(first, TextField.class, spec -> spec.withCaption("Age")), "45");
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

Vaadin Button contains the `click()` method, however that method is not well fit for testing:

* If the button is disabled, it will silently do nothing instead firing the click event. However, when writing the test,
  we expect the button to be enabled and fully able to receive (and execute) clicks. In this case, an attempt to click such button
  from a test should fail.
* If the button is effectively invisible (it may be visible itself, but it's nested in a layout that's invisible), the `click()` method
  will still run the listeners even though the user can't really interact with the button. In this case, the `click()` method
  should fail as well.
 
It is therefore important that we use the `Button._click()` extension method provided by the Karibu Testing library, which checks
all the above points, prior running the click listeners (in Java it's `_click(button)` since Java doesn't have extension methods).

### Changing values

The `HasValue.setValue()` function succeeds even if the component in question is disabled or read-only. However, when we
want to simulate user input and we want to change the value of, say, a `ComboBox`, we expect the ComboBox to be enabled,
read-write, visible; in other words, fully prepared to receive user input.

It is therefore important to use the `HasValue._value` extension property provided by the Karibu Testing library, which checks
all the above items prior setting the new value. In Java it's `_setValue(hasValue, value)` since Java doesn't have extension methods.

### Support for Grid

The Vaadin Grid is the most complex component in Vaadin, and therefore it requires a special set of testing methods, to assert the state and
contents of the Grid.

* You can retrieve a bean at particular index; for example `grid._get(0)` will return the first item. (in Java it's `GridKt.get(grid, 0);`).
* You can check for the total amount of items shown in the grid, by calling `grid._size()` (or Java: `GridKt._size(grid);`).
* You can click a button at a particular column (or any `ClickableRenderer` for that matter), by calling `grid._clickRenderer(0, "actions")`
  (Java: `GridKt._clickRenderer(grid, 0, "actions");`).
* You can obtain a full formatted row as seen by the user, by calling `grid._getFormattedRow(rowIndex)` - it will return that particular row as
  `List<String>`. In Java it's `GridKt._getFormattedRow(grid, 5);`.
* You can assert on the number of rows in a grid, by calling `grid.expectRows(25)`. If there is a different amount of rows, the function will
  fail and will dump first 10 rows of the grid, so that you can see the actual contents of the grid. In Java it's `GridKt.expectRows(grid, 25);`.
* You can assert on a formatted output of particular row of a grid: `grid.expectRow(rowIndex, "John Doe", "25")`. If the row looks different,
  the function will fail with a proper grid dump. In Java it's `GridKt.expectRow(new Grid<>(), 0, "John Doe", "25");`.

## Adding support for custom search criteria

> *Note*: Java doesn't have extension methods and thus this feature is not supported in Java.

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
