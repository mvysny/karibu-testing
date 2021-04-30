[![GitHub tag](https://img.shields.io/github/tag/mvysny/karibu-testing.svg)](https://github.com/mvysny/karibu-testing/tags)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.mvysny.kaributesting/karibu-testing-v10/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.mvysny.kaributesting/karibu-testing-v10)

# Getting started: Karibu-Testing on Vaadin Flow

Compatibility chart:

| Karibu-Testing version | Supported Java | Supported Vaadin version |
|------------------------|----------------|--------------------------|
| 1.0.x (unmaintained) | Java 1.8+ | Vaadin 10, Vaadin 11, Vaadin 12 |
| 1.1.x (unmaintained) | [Java 1.8-11](https://github.com/mvysny/karibu-testing/issues/31) | Vaadin 13, Vaadin 14 (bower mode, deprecated) |
| 1.2.0+ (unmaintained) | Java 1.8+ | Vaadin 16-18 |
| 1.2.3+ (unmaintained) | Java 1.8+ | Vaadin 14.3 and higher in (p)npm mode; Vaadin 16-18 |
| 1.2.11+ | Java 1.8+ | Vaadin 14 (only 14.3+) in (p)npm mode; Vaadin 18-19+ |
| 1.3.0+ (recommended) | Java 1.8+ | Vaadin 14 (only 14.6+) in (p)npm mode; Vaadin 19+ |

To start, just add the following lines into your Gradle `build.gradle` file:

```groovy
repositories {
    mavenCentral()
}
dependencies {
    testCompile "com.github.mvysny.kaributesting:karibu-testing-v10:x.y.z"
}
```

> Note: obtain the newest version from the tag name above

For Groovy, use `testCompile "com.github.mvysny.kaributesting:karibu-testing-v10-groovy:x.y.z"`
instead. Groovy support has been added starting from Karibu-Testing 1.1.20 and higher, and 1.2.1 and higher.
If you're using Pro components, use `testCompile "com.github.mvysny.kaributesting:karibu-testing-v10-pro-groovy:x.y.z"` instead.

For Maven it's really easy: Karibu-Testing is published on Maven Central, so all you need to do is to add the dependency
to your `pom.xml`:

```xml
<project>
	<dependencies>
		<dependency>
			<groupId>com.github.mvysny.kaributesting</groupId>
			<artifactId>karibu-testing-v10</artifactId>
			<version>x.y.z</version>
			<scope>test</scope>
		</dependency>
    </dependencies>
</project>
```

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

Kotlin:
```kotlin
class MyUITest : DynaTest({
    test("proper greeting") {
        val label = GreetingLabel()
        label.greet("world")
        expect("Hello, world") { label.text }
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
        assertEquals("Hello, world", label.getText());
    }
})
```

Groovy:
```groovy
class MyUITest {
    @Test
    void testProperGreeting() {
        GreetingLabel label = new GreetingLabel()
        label.greet("world")
        assertEquals("Hello, world", label.getText())
    }
})
```

Nothing special here - we have just instantiated the component as we would a
regular Java object, and then we asserted that the text is updated properly.
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

> **Note:** With Groovy you can build your Vaadin UI using the [Vaadin Groovy Builder](https://github.com/mvysny/vaadin-groovy-builder) library.

In order to test this app, we need to instantiate and initialize an `UI`.
In order to properly initialize the `UI` class, a proper Vaadin
environment needs to be prepared:

* We need to prepare the `VaadinSession` in a way that `VaadinSession.getCurrent()` returns a proper session
* We need to run the testing code with Vaadin lock obtained
  (since we're going to invoke Vaadin components and that can only be done
  on the UI thread)
* We need to create the UI instance and initialize it properly - besides other
  things we need to call the `UI.doInit()` method.

Luckily, this is exactly what the `MockVaadin.setup()` function does. It will prepare everything for us and even initialize the `UI`; we just need
to provide the auto-detected set of `@Route`s to the function:

Kotlin:
```kotlin
class MyUITest : DynaTest({
    // initialize routes only once, to avoid view auto-detection before every test and to speed up the tests
    lateinit var routes: Routes
    beforeGroup { routes = Routes().autoDiscoverViews("com.vaadin.flow.demo") }
    beforeEach { MockVaadin.setup(routes) }
})
```
Java, Groovy:
```java
public class MyUITest {
    private static Routes routes;

    @BeforeAll
    public static void createRoutes() {
        // initialize routes only once, to avoid view auto-detection before every test and to speed up the tests
        routes = new Routes().autoDiscoverViews("com.vaadin.flow.demo");
    }
    
    @BeforeEach
    public void setupVaadin() {
        MockVaadin.setup(routes);
    }
}
```

> **Tip for Kotlin users:** We're using the [DynaTest](https://github.com/mvysny/dynatest)
testing framework which runs on top of JUnit5. You can of course use whichever
testing library you prefer.

We can verify that everything is prepared correctly, simply by obtaining the current UI contents and asserting that it is a `MainView` (since our
simple testing app uses `MainView` as the root route):

Kotlin:
```kotlin
class MyUITest : DynaTest({
    lateinit var routes: Routes
    beforeGroup { routes = Routes().autoDiscoverViews("com.vaadin.flow.demo") }
    beforeEach { MockVaadin.setup(routes) }
    test("simple UI test") {
        val main = UI.getCurrent().children.findFirst().get() as MainView
        expect(2) { main.children.count() }
    }
})
``` 

Java:
```java
public class MyUITest {
    private static Routes routes;

    @BeforeAll
    public static void createRoutes() {
        routes = new Routes().autoDiscoverViews("com.vaadin.flow.demo");
    }

    @BeforeEach
    public void setupVaadin() {
        MockVaadin.setup(routes);
    }
    
    @Test
    public void simpleUITest() {
        final MainView main = (MainView) UI.getCurrent().getChildren().findFirst().get();
        assertEquals(2, main.getChildren().count());
    }
}
```

### Simulating the user input

We can now examine and assert on the MainView's properties, and more importantly,
discover its children (the `ExampleTemplate` and `Button`, respectively).
When we obtain the `Button` instance, we can simply call the server-side `click()` API on it, to simulate a click on the button itself.
The `click()` method will execute all listeners and will block until
all listeners are done; we can check that the click listener was run and it had changed the value
of the template, by examining the value of `ExampleTemplate`.

> Note: as you'll learn later on, neither `Button.click()` nor `setValue()` will check for whether the component is enabled or not.
Therefore it's important to use `button._click()` and `textField._value = "foo"` instead (Java: `_click(button)` and `_setValue(textField, "foo")`).

Obtaining the `Button` in this simple project is easy - it's the first child of the `MainView`
so we can simply call `children.findFirst().get() as Button` to obtain the button.
However, typical Vaadin apps has much more complex structure with lots of nested layouts.
We need some kind of a lookup function which will find the appropriate component for us.

### Looking up the components

The Karibu-Testing library provides three functions for this purpose; for now we are only interested in one of them:

* `_get<type of component> { criteria }` will find exactly one component of given type, matching given criteria, in the current UI. The function will fail
  if there is no such component, or if there are too many of matching components. For example: `_get<Button> { caption = "Click me" }`.
  In Java you need to `import static com.github.mvysny.kaributesting.v10.LocatorJ.*;`; then you can call
  `_get(Button.class, spec -> spec.withCaption("Click me"));`. In Groovy you'll need
  to `import static com.github.mvysny.kaributesting.v10.groovy.LocatorG.*;`; then you can call
    `_get(Button) { caption = "Click me" }` 

This particular function will search for all components nested within `UI.getCurrent()`.
You can call the function in a different way, which will restrict the search to some particular layout
which is handy when you're testing a standalone custom UI component outside of the `UI` class:

* `component._get<type of component> { criteria }` will find exactly one component of given type amongst the `component` and all of its children and descendants.
  In Java: `_get(component, Button.class, spec -> spec.withCaption("Click me"));`.
  In Groovy: `component._get(Button) { caption = "Click me" }`

> **Info:** `_get<Button> { caption = 'Click me' }` is merely an shorthand for `UI.getCurrent()._get<Button> { caption = 'Click me' }`,
  or for `_get(UI.getCurrent(), Button.class, spec -> spec.withCaption("Click me"));`

With this arsenal at hand, we can rewrite the test:

Kotlin:
```kotlin
class MainViewTest: DynaTest({
    lateinit var routes: Routes
    beforeGroup { routes = Routes().autoDiscoverViews("com.vaadin.flow.demo") }
    beforeEach { MockVaadin.setup(routes) }

    test("test greeting") {
        // simulate a button click as if clicked by the user
        _get<Button> { caption = "Click me" } ._click()

        // look up the Example Template and assert on its value
        expect("Clicked!") { _get<ExampleTemplate>().value }
    }
})
``` 

Java:
```java
import static com.github.mvysny.kaributesting.v10.LocatorJ.*;
public class MyUITest {
    private static Routes routes;

    @BeforeAll
    public static void createRoutes() {
        routes = new Routes().autoDiscoverViews("com.vaadin.flow.demo");
    }

    @BeforeEach
    public void setupVaadin() {
        MockVaadin.setup(routes);
    }

    @Test
    public void testGreeting() {
        // simulate a button click as if clicked by the user
        _click(_get(Button.class, spec -> spec.withCaption("Click me")));

        // look up the Example Template and assert on its value
        assertEquals("Clicked!", _get(ExampleTemplate.class).getValue());
    }
}
```

Groovy:
```groovy
import static com.github.mvysny.kaributesting.v10.groovy.LocatorG.*;
@CompileStatic class MyUITest {
    private static Routes routes

    @BeforeAll
    static void createRoutes() {
        routes = new Routes().autoDiscoverViews("com.vaadin.flow.demo")
    }

    @BeforeEach
    void setupVaadin() {
        MockVaadin.setup(routes)
    }

    @Test
    void testGreeting() {
        // simulate a button click as if clicked by the user
        _get(Button) { caption = "Click me" } ._click()

        // look up the Example Template and assert on its value
        assertEquals("Clicked!", _get(ExampleTemplate)._value)
    }
}
```

> **Important note:** The lookup methods will only consider *visible* components - for example `_get<Button>()` will fail if the
  "Click me" button is invisible. This is because the intent of the test is to populate/access the components as if it was the user who
  is accessing the application; and of course the user can't access the component if it is invisible.

## Example projects

The [Vaadin 10 Karibu-DSL Helloworld Application](https://github.com/mvysny/karibu10-helloworld-application)
is a very simple Kotlin-based project consisting of just
one view and a single test for that view.

The [Vaadin Groovy Builder Example App](https://github.com/mvysny/vaadin-groovy-builder-example)
is a very simple Groovy-based project consisting of just
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

Kotlin:
```kotlin
class MyUITest : DynaTest({
    lateinit var routes: Routes
    beforeGroup { routes = Routes().autoDiscoverViews("com.vaadin.flow.demo") }
    beforeEach { MockVaadin.setup(routes) }
    test("simple test") {
        // navigate to the "Categories" list route.
        UI.getCurrent().navigateTo("categories")

        // now the "Categories" list should be attached to your UI and displayed. Look up the Grid and assert on its contents.
        val grid = _get<Grid<*>>()
        grid.expectRows(1)
        // etc etc
    }
})
```

Java:
```java
import static com.github.mvysny.kaributesting.v10.LocatorJ.*;
import static com.github.mvysny.kaributesting.v10.GridKt.*;
public class MyUITest {
    private static Routes routes;

    @BeforeAll
    public static void createRoutes() {
        routes = new Routes().autoDiscoverViews("com.vaadin.flow.demo");
    }

    @BeforeEach
    public void setupVaadin() {
        MockVaadin.setup(routes);
    }

    @Test
    public void testGreeting() {
        // navigate to the "Categories" list route.
        UI.getCurrent().navigateTo("categories");

        // now the "Categories" list should be attached to your UI and displayed. Look up the Grid and assert on its contents.
        final Grid<Person> grid = _get(Grid.class);
        expectRows(grid, 1);
        // etc etc
    }
})
```

Groovy:
```groovy
import static com.github.mvysny.kaributesting.v10.groovy.LocatorG.*
@CompileStatic class MyUITest {
    private static Routes routes

    @BeforeAll
    static void createRoutes() {
        routes = new Routes().autoDiscoverViews("com.vaadin.flow.demo")
    }

    @BeforeEach
    void setupVaadin() {
        MockVaadin.setup(routes)
    }

    @Test
    void testGreeting() {
        // navigate to the "Categories" list route.
        UI.current.navigateTo("categories")

        // now the "Categories" list should be attached to your UI and displayed. Look up the Grid and assert on its contents.
        Grid<Person> grid = _get(Grid)
        grid.expectRows(1)
        // etc etc
    }
})
```

### Polymer Templates / Lit Templates

Testing PolymerTemplates/LitTemplates with Karibu is a bit tricky.
The purpose of PolymerTemplates is to move as much code as possible to the
client-side, while Karibu is designed to test server-side code only. The child components are either
not accessible from the server-side altogether, or they are only
"shallow shells" of components constructed server-side - almost none of their properties
are transferred to the server-side.

For example, a Vaadin `Button` nested in a Template will have empty caption server-side,
even though the element clearly has a text client-side (and the caption shows properly in the browser).
Please read the discussion at [Can't look up components inside of PolymerTemplate](https://github.com/mvysny/karibu-testing/issues/1)
for a technical explanation of this phenomenon.

Also the Vaadin's `VerticalLayout` (and all other layouts) will not have any child
components present on the server-side, even though they're present in the template itself:
the `verticalLayout.getChildren()` will return an empty Stream.
That makes it impossible for Karibu-Testing to look up components inside of a Polymer Template.

Fear not! It is still possible to use Karibu-Testing with Polymer Templates.
To work around the look-up issue,
simply publish your `@Id`-annotated
fields as `public` or `internal` (Kotlin) or with package visibility (Java).

Also you can still invoke event listeners (such as button clicks) easily since
the event listeners are defined server-side.

Please see the following example code:

Kotlin:

```kotlin
@Route("reviews")
class ReviewsList : PolymerTemplate<TemplateModel>() {
    @Id("search")
    internal lateinit var search: TextField
    @Id("addReview")
    internal lateinit var addReview: Button
    // ...
}

test("create review") {
   // this doesn't work because of https://github.com/mvysny/karibu-testing/issues/1
   //  _get<Button> { caption = "New review" } ._click()
   
   // You'll need to look up the PolymerTemplate itself, then retrieve the button from the Kotlin field:
   _get<ReviewsList>().addReview._click()
}
```

Java:

```java
@Route("reviews")
public class ReviewsList extends PolymerTemplate<TemplateModel> {
    @Id("search")
    TextField search;
    @Id("addReview")
    Button addReview;
    // ...
}

@Test
public void createReviewTest() {
   // this doesn't work because of https://github.com/mvysny/karibu-testing/issues/1
   // _click(_get(Button, spec -> spec.withCaption("New review"));

   // You'll need to look up the PolymerTemplate itself, then retrieve the button from the Java field:
   ReviewsList list = _get(ReviewsList.class);
   _click(list.addReview);
}
```

#### Nested Polymer Templates

Sometimes you have a PolymerTemplate nested inside another PolymerTemplate, etc.
In order to access components from the inner PolymerTemplate, you will have to do
the following:

1. Use Karibu-Testing to discover the outermost PolymerTemplate
2. Now Karibu-Testing is unable to discover the children of that PolymerTemplate;
   you will therefore have to expose the inner PolymerTemplate via a field of the outer PolymerTemplate,
   then access the inner PolymerTemplate via that field.


#### PolymerTemplates which load stuff from the `node_modules` folder

(Requires Karibu-Testing 1.1.16 or higher)

If you try to use `JsModule(@foo/bar.js)` with your PolymerTemplate, Karibu-Testing
will try to load the JS file from the local `node_modules/` folder. That will usually
work in your dev env, but fail in your CI since `node_modules/` is not created.

In CI, you will thus receive the following error message:

> `node_modules` folder doesn't exist, cannot load template sources for <my-component> @mycomponent/my-component.js

or

> Can't load template sources for <my-component> @mycomponent/my-component.js. Please: ...

In such case, you need to run `mvn vaadin:build-frontend` goal (Maven) or
`gradle vaadinBuildFrontend` (Gradle) before
you run your tests, to populate the
`node_modules/` folder. Be sure to run `vaadin:build-frontend` goal after your classes
has been compiled (since build-frontend analyzes `*.class` files), otherwise the `node_modules/` folder
will simply be empty, and Karibu-Testing will still complain about the missing template file.

The best way is to run `mvn clean package vaadin:build-frontend verify` (Maven) or
`gradle clean vaadinBuildFrontend build -Pvaadin.productionMode` (Gradle) from your CI, to
run the plugins in correct order.

It is also handy to cache the `node_modules/` folder with your CI, since the folder is huge
and it takes 60 seconds to just populate it from the NPM Interwebz. For example with GitLab CI:

```yaml
cache:
  paths:
    - .m2
    - node_modules
```

Populating `node_modules/` requires the `node` and `npm` programs to be present.
However Vaadin 14.2.0 Maven plugin (Gradle plugin 0.7.0) will
install those for you automatically, there's no need for you to do anything.

#### LitElement

`LitElement`-based components are supported. After all, from the server-side perspective
they're just `Component`s since there's no `LitElement` server class.

## API

### Looking up components

This library provides three methods for looking up components.

* `_get<type of component> { criteria }` will find exactly one **visible** component of given type in the current UI, matching given criteria. The function will fail
  if there is no such component, or if there are too many of matching **visible** components. For example: `_get<Button> { caption = "Click me" }`.
  Java: `_get(Button.class, spec -> spec.withCaption("Click me"));`.
  Groovy: `_get(Button) { caption = "Click me" }`.
* `_find<type of component> { criteria }` will find a list of matching **visible** components of given type in the current UI. The function will return
  an empty list if there is no such component. For example: `_find<VerticalLayout> { id = "form" }`;
  Java: `_find(VerticalLayout.class, spec -> spec.withId("form"));`.
  Groovy: `_find(VerticalLayout) { id = "form" }`.
* `_expectNone<type of component> { criteria }` will expect that there is no **visible** component matching given criteria in the current UI; the function will fail if
  one or more components are matching. For example: `_expectNone<Button> { caption = "Delete" }`;
  Java: `_expectNone(Button.class, spec -> spec.withCaption("Delete"));`.
  Groovy: `_expectNone(Button) { caption = "Delete" }`.
* `_expectOne<type of component> { criteria }` will expect that there is
  exactly one **visible** component matching given criteria in the current UI; the function will fail if
  none, or more than one components are matching. For example: `_expectOne<Button> { caption = "Delete" }`. Java:
  `_assertOne(Button.class, spec -> spec.withCaption("Delete"));`.
  Groovy: `_assertOne(Button) { caption = "Delete" }`.
  Note: this is
  exactly the same as `_get()`, but it may communicate the intent of the test better in the case when you're
  only asserting that there is exactly one such component.
* `_expect<type of component> { criteria }` is a generic version of the above methods which asserts
  that all **visible** components match given criteria. For example, this checks that
  there are five buttons in a button bar: `buttonBar._expect<Button>(5)`.
  Note: this is exactly the same as `_find()`, but it may communicate the intent of the test better in the case when you're
  only asserting on the state of the UI.

> I can't stress the **visible** part enough. Often the dump will show the button, the caption will be correct and everything
  will look OK but the lookup method will claim the component is not there. The lookup methods only search for visible
  components - they will simply ignore invisible ones.

This set of functions operates on `UI.getCurrent()`. However,
often it is handy to test a component separately from the UI, and perform the lookup only
in that component. There are `Component._get()`, `Component._find()`,
`Component._expectNone()` and `Component._expect()` counterparts, added to every Vaadin
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

Groovy:
```kotlin
class AddNewPersonForm : VerticalLayout() {
    // nests fields, uses binder, etc etc
}

@Test void "add new person happy flow"() {
    def form = new AddNewPersonForm()
    form._get(TextField) { caption = "Name:" } ._value = "John Doe"
    form._get(Button) { caption = "Create" } ._click()
}
```

Of course this is not possible with Java since Java doesn't support extension methods. That's why
there are `_get()` and others that take the receiver component as the first parameter:

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


Such methods are also useful for example when locking the lookup scope into a particular container, say, some particular layout:
```kotlin
_get<FlexLayout> { id = "form" } ._get<TextField> { caption = "Age" } ._value = "45"
```
```groovy
_get(FlexLayout) { id = "form" } ._get(TextField) { caption = "Age" } ._value = "45"
```

Since there is no way to see the UI of the app with this kind of approach
(since there's no browser), the lookup functions will dump the component tree
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

### Useful Tips

* You should generally prefer `_get<Grid>()` over `_find<Grid>().get(0)`. If there are two or more Grids,
  the former code will make the test fail with a clear explanation, while the latter will simply
  select an arbitrary Grid, or fail with a non-informative `IndexOutOfBoundsException` if there is no such grid.
  Using `_get()` will therefore make your code more robust. If you know there are two grids and you need
  them both, then use `_find<Grid> { count = 2..2 }`
* If you're only asserting that there is such a component (e.g. that a button named "Cancel" exists and is visible),
  it's better to use `_expectOne()` (Java: `LocatorJ._assertOne()`) to express this intent explicitly, instead of
  simply calling `_get()` without doing anything with the result component.

### Customizing dump for your components

The `dump()` function uses the `toPrettyString()` function to print all basic information
about a Vaadin component: the visibility, read-only-ness, value, whether the component is enabled or not, etc.
See the above dump for an example for a
`TextField` and a `Button`.

However, `toPrettyString()` will not print any custom state your component may have, which may be vital for debugging purposes.
That's where the `prettyStringHook` will come handy.
Since Karibu-Testing 1.1.29/1.2.2 you can assign a function to
`prettyStringHook` in Kotlin (`PrettyPrintTreeKt.setPrettyStringHook()` in Java/Groovy).
The function receives the component being pretty-printed, and a mutable list. You can
add as many String information as you like about the component into the list:
everything added to the list will be pretty-printed by the `toPrettyString()` function.

For example if your custom component has an icon, you can add the following item to the list:
`list.add("icon='$icon'")`.

### Clicking Buttons

Vaadin Button contains the `click()` method, however that method actually invokes the browser-side click method which will then eventually
fire server-side click listeners. However, with browserless testing there is no browser and nothing gets done.

It is therefore important that we use the `button._click()` extension method provided by the Karibu Testing library, which moreover
checks the following points prior running the click listeners:

* When writing the test,
  we expect the button to be enabled and fully able to receive (and execute) clicks. In this case, an attempt to click such button
  from a test will fail.
* If the button is effectively invisible (it may be visible itself, but it's nested in a layout that's invisible), the user can't really
  interact with the button. In this case, the `_click()` method will fail as well.

The above approach works with Kotlin and Groovy. With Java, you simply call `_click(button)`.

### Changing values

The `HasValue.setValue()` function succeeds even if the component in question is disabled or read-only. However, when we
want to simulate user input and we want to change the value of, say, a `Combobox`, we expect the Combobox to be enabled,
read-write, visible; in other words, fully prepared to receive user input.

It is therefore important to use the `HasValue._value` extension property
provided by the Karibu Testing library, which checks
all the above items prior setting the new value:

* Kotlin, Groovy: `textField._value = "42"`
* Java: `LocatorJ._setValue(textField, "42");`

Changing the value fires the ValueChangeEvent with `fromClient` set to false.
However, sometimes you need to fire a "client-side" value-change event,
to test a code reacting to such events. In such case please use:

* Kotlin, Groovy: `textField._fireValueChange()`
* Java: `LocatorJ._fireValueChange(textField)`

### Firing DOM Events

Listeners added via `Element.addEventListener()` API can be invoked easily:

Kotlin:
```kotlin
val div = Div()
div.element.addEventListener("click") { /* do something */ }
div._fireDomEvent("click")
```

Groovy:
```groovy
val div = new Div()
div.element.addEventListener("click") { /* do something */ }
div._fireDomEvent("click")
```

Java:
```java
BasicUtilsKt._fireDomEvent(new Div(), "click");
```

#### Notifying `@DomEvent` listeners

Using the above technique it's possible to also fire higher-level events annotated
with `@DomEvent`. For example, we will fire the `ClickEvent` which is annotated with `@DomEvent("click")`
and receives `screenX` as `@EventData("event.screenX")`:

```kotlin
val div = Div()
div._fireDomEvent("click", Json.createObject().apply { put("event.screenX", 20.0) })
div.addClickListener { e -> print(e.screenX) }
```

### Focus/Blur

Server-side can only track focus by listening on `FocusEvent` and `BlurEvent`. To simulate focus:
* Call `focusable._focus()`/`BasicUtilsKt._focus(focusable)` to fire the `FocusEvent` event;
* Call `focusable._blur()`/`BasicUtilsKt._blur(focusable)` to fire the `BlurEvent` event;

### Support for Grid

The Vaadin Grid is the most complex component in Vaadin, and therefore it requires a special set of testing methods, to assert the state and
contents of the Grid.

* You can retrieve a bean at particular index; for example `grid._get(0)` will return the first item (Kotlin, Groovy).
  Java: you need to `import static com.github.mvysny.kaributesting.v10.GridKt.*;`, then you can call `_get(grid, 0);`.
* You can check for the total amount of items shown in the grid, by calling `grid._size()` (Kotlin, Groovy). Java: `_size(grid);`
* You can obtain a full formatted row as seen by the user, by calling `grid._getFormattedRow(rowIndex)` - it will return that particular row as
  `List<String>`. In Java: `_getFormattedRow(grid, rowIndex)`
* You can assert on the number of rows in a grid, by calling `grid.expectRows(25)`. If there is a different amount of rows, the function will
  fail and will dump first 10 rows of the grid, so that you can see the actual contents of the grid.
  In Java: `expectRows(grid, 25)`
* You can assert on a formatted output of particular row of a grid: `grid.expectRow(rowIndex, "John Doe", "25")`. If the row looks different,
  the function will fail with a proper grid dump.
* Use `grid._clickItem(rowIndex)`/`GridKt._clickItem(grid, rowIndex)` to fire a listener added via `addItemClickListener()`.
* Use `grid._doubleClickItem(rowIndex)`/`GridKt._doubleClickItem(grid, rowIndex)` to fire a listener added via `addItemDoubleClickListener()`.

#### Clicking Renderers

You can use `grid._clickRenderer(0, "edit")` to click a `NativeButtonRenderer`
or a `Button`/`ClickNotifier` component produced by `ComponentRenderer` (Java: `GridKt._clickRenderer(0, "edit")`).

If your `ComponentRenderer` produces something else than a `Button` or a `ClickNotifier`,
please use the `grid._getCellComponent()` function instead (since Karibu-Testing 1.2.12):

Java:
```java
((Checkbox) GridKt._getCellComponent(grid, 0, "foo")).setValue(true);
```

Kotlin:
```kotlin
(grid._getCellComponent(0, "foo") as Checkbox)._value = true
```

Please see the `GridKt` class for more details.

#### Grid Filters

The filtering code is not called when your code calls `DataProvider.refreshAll()`,
and it's not obvious why.

The gist of the problem is as follows: when the Vaadin app is running with the full client-side code, then the `dataProvider.setFilter()`
calls `dataProvider.refreshAll()` at some point. That call informs the client-side
Grid code that it needs to throw away all data and ask for a fresh data.
Grid then makes a request to Vaadin Servlet, which redirects the call to
`DataProvider`'s data fetching code, which in turn applies the filters.

With Karibu-Testing there is no client-side, therefore `dataProvider.refreshAll()`
does nothing since there is no client-side that can re-fetch the data, and
thus `DataProvider`'s fetching code does not get called.

However, there is another possibility to test the filters:
they are invoked every time you call one of
Karibu-Testing's `GridKt` methods such as `expectRows()` or `expectRow()` or `_clickRenderer()`,
or one of the "lower-level" functions `_get(0)` or `_size()` or `_getFormattedRow()`.

The reasoning is that in this particular case the unit-test itself can "play the
role of the client-side": after the filter has been modified, the most reasonable
thing for the test to do is to assert on the rows of the Grid;
Karibu-Testing will then call `DataProvider`'s data fetching
methods which will in turn invoke the filters themselves.

#### Grid Sorting

The same thing applies to Grid sorting: the `Grid.sort()` methods does not trigger
the `DataProvider`'s fetching methods. However, the sorting is
automatically applied by Karibu-Testing as if by the Grid itself,
when you call Karibu-Testing's `GridKt` methods.

### IronList

Similar to Grid, but one column only, no sorting, no filtering, no header,
good for lazy list of items akin to Android's ListView.

* You can retrieve a bean at particular index; for example `ironList._get(0)` will return the first item (Kotlin, Groovy).
  Java: you need to `import static com.github.mvysny.kaributesting.v10.IronListKt.*;`, then you can call `_get(ironList, 0);`.
* You can check for the total amount of items shown in the list, by calling `ironList._size()` (Kotlin, Groovy). Java: `_size(ironList);`
* You can obtain a full formatted row as seen by the user, by calling `ironList._getFormattedRow(rowIndex)` - it will return that particular row as
  `String`. In Java: `_getFormattedRow(ironList, rowIndex)`
* You can assert on the number of rows in the list, by calling `ironList.expectRows(25)`. If there is a different amount of rows, the function will
  fail and will dump first 10 rows of the list, so that you can see the actual contents of the ironList.
  In Java: `expectRows(ironList, 25)`
* You can assert on a formatted output of particular row of the iron list: `ironList.expectRow(rowIndex, "John Doe")`. If the row looks different,
  the function will fail with a proper list dump.

See `IronListKt` class for more details.

### Support for Upload

An entire upload lifecycle is mocked properly. Simply call the following to mock-upload a file:

Kotlin:
```kotlin
upload._upload("hello.txt", "Hello world!".toByteArray())
```

Groovy:
```groovy
upload._upload("hello.txt", "Hello world!".bytes)
```

Java:
```java
UploadKt._upload(upload, "hello.txt", "Hello world!".getBytes());
```

Invokes `StartedEvent`, then feeds given file to the `Upload.receiver`, then
invokes the `SucceededEvent` and `FinishedEvent` listeners.
Doesn't call `ProgressListener`.

If writing to the receiver fails, `FailedEvent` is invoked instead of `SucceededEvent`, then
the exception is re-thrown from this function, so that the test fails properly.

To quickly test failure handling, simply call `_uploadFail()` instead.
The function will first invoke `StartedEvent`, then poll
`Upload.receiver` and closes it immediately without writing anything, then
fire `FailedEvent` and `FinishedEvent`.

### Support for ComboBox/Select

You can use static methods from `ComboBox.kt` to assert on the state of the combo box.
For example, you can call `comboBox.getSuggestions()`/`ComboBoxKt.getSuggestions()` to return the current list
of dropdown suggestions, formatted according to the current `ItemLabelGenerator`.

You can also use `comboBox.setUserInput()`/`ComboBoxKt.setUserInput()` to simulate user typing into the ComboBox,
filtering the suggestions.

You can also use `comboBox._fireCustomValueSet()`/`ComboBoxKt._fireCustomValueSet()` to simulate user creating a
new item in the ComboBox.

### Downloading Anchor/Image Contents

Call `anchor._download()`/`DownloadKt._download(anchor)` to download contents of the `StreamResource` to which the Anchor points to.
Call `image.download()`/`DownloadKt.download(image)` to download contents of the `StreamResource` to which the Image points to.

### Support for ContextMenu

It's currently not possible to retrieve `ContextMenu` from the component it is attached to,
nor `GridContextMenu` from the Grid it is attached to. Please add your vote for adding support for this
to Vaadin: [https://github.com/vaadin/vaadin-context-menu-flow/issues/43](https://github.com/vaadin/vaadin-context-menu-flow/issues/43).

As a workaround, you will have to remember references to ContextMenu in your views and components,
and retrieve them via getters. Then, it's very easy to click on a menu item; simply call

Kotlin, Groovy:
```kotlin
contextMenu._clickItemWithCaption("Save")
gridContextMenu._clickItemWithCaption("Delete", person)
```

Java:
```java
ContextMenuKt._clickItemWithCaption(contextMenu, "Save");
ContextMenuKt._clickItemWithCaption(gridContextMenu, "Delete", person);
```

### Support for MenuBar

It's very easy to click on a menu item; simply call

Kotlin, Groovy:
```kotlin
menuBar._clickItemWithCaption("Save")
```

Java:
```java
ContextMenuKt._clickItemWithCaption(menuBar, "Save");
```

### Support for LoginForm and LoginOverlay

The username/password text fields are not accessible from server-side so we can't
fill those in directly. Instead we'll simply fire the login event as follows:

Kotlin:
```kotlin
_get<LoginOverlay>()._login("nbu", "nbusr123")
_get<LoginOverlay>()._forgotPassword()  // simulate the "forgot password" click
```

Groovy:
```groovy
_get(LoginOverlay)._login("nbu", "nbusr123")
_get(LoginOverlay)._forgotPassword()  // simulate the "forgot password" click
```

Java:
```java
LoginFormKt._login(_get(LoginOverlay.class), "nbu", "nbusr123");
LoginFormKt._forgotPassword(_get(LoginOverlay.class));
```

### Support for fields nested in FormLayout.FormItem

Suppose you have the following form:

```kotlin
class AddressPanel : FormLayout() {
    init {
        formItem("Primary Address") {
            checkBox()
        }
        formItem("Street") {
            textField()
        }
    }
}
```

You can easily retrieve the fields nested in FormItems as follows:

```kotlin
val isPrimary = _get<FormLayout.FormItem> { caption = "Primary Address" } .field as Checkbox
expect(true) { isPrimary._value }
```

### Support for RouterLink

See the `RouterLinkKt` class for a list of utility methods for the `RouterLink` component.
Currently there's just one: `_click()` which simply calls `UI.navigate()` under the hood:

kotlin, groovy:
```kotlin
customersLink._click()
```

Java:
```java
RouterLinkKt._click(customersLink);
```

## Adding support for custom search criteria

> *Note*: this feature is unsupported for Java since Java lacks extension methods.

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

Groovy: Similar thing can be done by using the Groovy extension methods mechanism.
Simply add the `setPrimary(boolean)` extension function to the `SearchSpec` class.
See [Groovy Extension Modules Example](https://mrhaki.blogspot.com/2013/01/groovy-goodness-adding-extra-methods.html)
for more details.

## Speed+Performance Optimizations

The `Routes.autoDiscoverViews("com.vaadin.flow.demo")` walks through all classes and examines
them for view annotations. This can potentially take 50ms for every test, which adds up in the end.
The best way is to discover the views only once; after all, the set of views is static and doesn't
typically change. In order to do that, calculate the

```
val routes: Routes = Routes.autoDiscoverViews("com.vaadin.flow.demo")
```

only once, e.g. in `beforeClass{}` or `@BeforeAll`-annotated method, and store the resulting
routes in a static field.

Another thing that takes up a lot of time is to initialize the PWA (especially the icons)
in the `PwaRegistry` class. That can take up to 1-2 seconds on every test. That's why
by default Karibu-Testing 1.1.19+ configures Vaadin to ignore the `@PWA` annotation and
does not initialize the `PwaRegistry`. However, if you need this functionality for some reason,
simply set `Routes.skipPwaInit` to `false`.

## Using Karibu-Testing with Spring

Karibu-Testing offers basic support for Spring.
Please see [t-shirt shop example](https://github.com/mvysny/t-shirt-shop-example) for
an example on how to use Karibu-Testing with a Spring app.

Note that Spring Security is not supported: Spring Security uses Servlet Filter
which requires Servlet Container to be up and running, yet Karibu-Testing doesn't
start any Servlet Container. See [Issue #47](https://github.com/mvysny/karibu-testing/issues/47)
for more details. The workaround is to [Manually Authenticate User with Spring Security](https://www.baeldung.com/manually-set-user-authentication-spring-security),
before navigating to a view. 

## Plugging into the testing lifecycle

Sometimes you need to wait a bit before the UI is ready and settled. For example if you have a "Send" button
which sends a chat message asynchronously, you need to wait after `_click()`-ing on the button before asserting
on the state of the chat component. That's something you can do in that one particular test by hand.
Hoever, imagine that your app uses async and push heavily; say that all of your data fetching code runs asynchronously.
You would have to await for async to finish basically after every UI component lookup.

We have added support to implement this kind of behavior. There is a global variable named `testingLifecycleHook`, which
contains hooks which by default do nothing. You can simply provide your own custom implementation of `TestingLifecycleHook`
interface which would await for async, and then set it to the `testingLifecycleHook` global variable.

## Mocking Server Request End Properly

Since Karibu-Testing runs in the same JVM as the server and there is no browser, the boundaries between the client and
the server become unclear. When looking into sources of any test method, it's really hard to tell where exactly the server request ends, and
where another request starts. However, it is very important to know the boundary, for example to understand when to run the `UI.access()` blocks.

You can establish an explicit client boundary in your test, by explicitly calling `MockVaadin.clientRoundtrip()`. However, since that
would be both laborous and error-prone, the default operation is that Karibu Testing pretends as if there was a client-server
roundtrip before every component lookup
via the `_get()`/`_find()`/`_expectNone()`/`_expectOne()`/`_expect()` call.
Therefore, `MockVaadin.clientRoundtrip()` is called from `TestingLifecycleHook.awaitBeforeLookup()` by default.

You can change this behavior by providing your own `TestingLifecycleHook` implementation as described above.

# Advanced Topics

## Testing Asynchronous Application

Some apps tend to use async heavily: by enabling Vaadin Push you can have the client notified about important stuff at any time.
You can even employ async data fetching, so that the threads in the JVM are not laying dormant doing nothing but taking
memory until the data is fetched.

The thing is that Karibu Testing runs the test with the UI lock held. That simplifies
testing very much, but that also prevents async updates by another thread, simply
because the test is holding the lock!

The solution is to briefly let loose of the UI lock in the testing thread,
allowing `UI.access()` tasks posted from a background thread to be processed.
Then the testing thread will re-obtain the lock and continue testing.

### Async Example

You can see this technique in use in the [vaadin-coroutines-demo](https://github.com/mvysny/vaadin-coroutines-demo)
sample project. What the test does is that it simply runs the long-running
background process when the `Buy Ticket` button is pressed. Then,
the test checks that a dialog has popped up. Here is the source of the test method:

```kotlin
_get<Button> { caption = "Buy Ticket" } ._click()
MockVaadin.runUIQueue()
Thread.sleep(200)
MockVaadin.runUIQueue()
expect("There are 25 available tickets. Would you like to purchase one?") { _get<ConfirmDialog>().message }
```

The test clicks the button, sleeps for a 200 millis (the request only takes 50 ms and
should be done by then), then calls `MockVaadin.runUIQueue()` which releases
the UI lock, runs the tasks and reacquires the lock. Meanwhile, the data-fetching
process which runs in the background fetches the data and posts a UI task
that shows a confirmation dialog. The dialog is then shown because
`MockVaadin.runUIQueue()` runs all submitted tasks and blocks until all the
tasks have been processed.

### Running the UI Queue Automatically

Calling `MockVaadin.runUIQueue()` manually in every test can be tedious. It is
easy to
forget to call the method, which results in mysterious test crashes. The easiest
way is to take advantage of Karibu-Testing hooking mechanism, and simply invoke
the `MockVaadin.runUIQueue()` before and after every component lookup:

```kotlin
object UIQueueRunnerHook : TestingLifecycleHook {
    override fun awaitBeforeLookup() {
        MockVaadin.runUIQueue()
    }
    override fun awaitAfterLookup() {
        MockVaadin.runUIQueue()
    }
}

beforeGroup { testingLifecycleHook = UIQueueRunnerHook }
```

## Cookies

Testing cookies is simple. To insert mock cookies into the request, simply fill them into the `MockRequest`
that backs up the `VaadinRequest` as follows:

Kotlin:
```kotlin
currentRequest.mock.addCookie(Cookie("foo", "bar"))
```

Java:
```java
MockVaadinKt.getMock(VaadinRequest.getCurrent()).addCookie(new Cookie("foo", "bar"));
```

To assert that your code has produced a cookie and written it into the response, simply use the `MockResponse`
methods:

Kotlin:
```kotlin
expect("bar") { currentResponse.mock.getCookie("foo").value }
```

Groovy (since Karibu-Testing 1.1.21/1.2.1):
```groovy
expect("bar") { VaadinResponse.current.mock.getCookie("foo").value }
```

Java:
```java
assertEquals("bar", MockVaadinKt.getMock(VaadinResponse.getCurrent()).getCookie("foo").getValue());
```

## Notifications

Testing notifications is easy - just take advantage of the `expectNotifications()`,
`expectNoNotifications()` and `clearNotifications()` functions
as in the following example:

```kotlin
Notification.show("Error")
expectNotifications("Error")
// expectNotifications also clears current notifications so that any further notifications won't be mixed with existing ones
expectNoNotifications()
```

For Java and Groovy, those functions are present in the `NotificationsKt` class.

## Preparing Mock Environment For `UI.init()`

Sometimes you already need to check for cookies in your `UI.init()` method. Since the `UI.init()` is called
from `MockVaadin.setup()`, you can not set up mock cookies after `MockVaadin.setup()` has run.
However, since `MockVaadin.setup()` also sets up mock request, you can not set up mock cookies
before `MockVaadin.setup()`. The only way is therefore to setup mock cookies *during* the `MockVaadin.setup()`
invocation, in the closure which creates the UI instance:

Kotlin:
```kotlin
MockVaadin.setup(uiFactory = {
    currentRequest.mock.addCookie(Cookie("foo", "bar"))
    MyUI()
})
```

Java:
```java
MockVaadin.setup(new Routes(), () -> {
    MockVaadinKt.getMock(VaadinRequest.getCurrent()).addCookie(new Cookie("foo", "bar"));
    return new MyUI();
});
```

## Bower versus NPM mode

Vaadin 13 used Bower+webjars to manage JavaScript dependencies; Vaadin 14 switched to
the npm+webpack management but still supports Bower+webjars dependency management.
You can read more about both modes in the [Vaadin 13 -> Vaadin 14 migration guide](https://vaadin.com/docs/v14/flow/v14-migration/v14-migration-guide.html).

Karibu-Testing supports both modes.

## Checking for Vaadin Versions

The `VaadinMeta.version: Int` provides the current Vaadin version, e.g. `13` for Vaadin 13, `14` for Vaadin 14 or later.

## Firing Keyboard Shortcuts

(since Karibu-Testing 1.2.9): You can call

```kotlin
fireShortcut(Key.SPACE, KeyModifier.CONTROL, KeyModifier.ALT)
```

to fire a shortcut action registered under the key modifier of "Ctrl+Alt+Space".
For Java/Groovy it's `ShortcutsKt.fireShortcut(Key.SPACE, KeyModifier.CONTROL, KeyModifier.ALT)`.

Usually the app will use the shortcut to perform a click or focus, but you can also use `Shortcuts.addShortcutListener()`
or Karibu-DSL's `currentUI.addShortcut(Ctrl + Alt + SPACE) {}` to run an arbitrary
block of code.

## Using custom ErrorHandler

Karibu-Testing has been designed to fail fast and pass the exception thrown by event handlers
directly to JUnit. This is the easiest way you can learn about errors in your code.
Unfortunately this also makes testing of the ErrorHandler harder. However, there is a way.

In order to test your custom ErrorHandler, you can simply throw an exception in `UI.access()`,
then call `MockVaadin.runUIQueue(true)`. The `runUIQueue()` method runs all outstanding
blocks scheduled via `UI.access()`; setting the `propagateExceptionToHandler` parameter to true
causes the `runUIQueue()` method to pass all exceptions to the current ErrorHandler
(set to current VaadinSession):

```kotlin
UI.getCurrent().access { throw RuntimeException("Simulated") }
MockVaadin.runUIQueue(true)
// say that your custom ErrorHandler shows a notification in case of exception...
// test that.
expectNotifications("An application error #6 occurred, please see application logs for details")
```

# Support for Vaadin Pro Components

## Grid Pro

[Vaadin Grid Pro](https://vaadin.com/components/vaadin-grid-pro)

It is possible to mock the inline Grid Pro editor and invoke your `ItemUpdater`s
as follows:

Kotlin:
```kotlin
val person = Person(...)
grid._proedit(person) {
  nameColumn._text("John")
  aliveColumn._checkbox(true)
}
expect("John") { person.name }
expect(true) { person.isAlive }
```

Java:
```java
GridProKt._proedit(grid, person, it -> {
    it._text(nameColumn, "John");
    it._checkbox(aliveColumn, "true");
});
```

Groovy: unsupported at the moment.

## Confirm Dialog

[Vaadin Confirm Dialog](https://vaadin.com/components/vaadin-confirm-dialog)

Kotlin:
```kotlin
_get<ConfirmDialog>()._fireConfirm()
_get<ConfirmDialog>()._fireCancel()
_get<ConfirmDialog>()._fireReject()
```

Groovy (since KT 1.1.21 / 1.2.1):
```groovy
_get(ConfirmDialog)._fireConfirm()
_get(ConfirmDialog)._fireCancel()
_get(ConfirmDialog)._fireReject()
```

Java:
```java
ConfirmDialogKt._fireConfirm(_get(ConfirmDialog.class));
```

# MPR (Multi-Platform Runtime)

It is possible to use Karibu-Testing to test [Vaadin MPR](https://vaadin.com/docs/v14/mpr/Overview.html)-based apps.
The [Vaadin14 MPR Gradle Demo](https://gitlab.com/mvysny/vaadin14-mpr-gradle-demo)
project demoes this possibility in the
[MyUITest](https://gitlab.com/mvysny/vaadin14-mpr-gradle-demo/-/blob/master/src/test/kotlin/org/test/MyUITest.kt)
class.

# Parallel testing with multiple UIs

The testing scenario here is to simulate two users, using the same
Vaadin app with two separate UIs at the same time.

The UI, VaadinSession and all other Vaadin objects are tied to the thread
who called `MockVaadin.setup()`. Therefore, it is possible to have multiple UIs
by starting two (or more) threads from the test method, then
calling `MockVaadin.setup()` from all of those threads.

**WARNING:** Make sure you're using Karibu-Testing 1.1.27+ or 1.2.1+, otherwise
the threads may randomly clear the Vaadin UI/Session instances (`UI.getCurrent()`
may randomly start to return null).

The example below uses an `ExecutorService` which manages a pool of four threads,
sending commands to those threads by the means of submitting jobs:

```kotlin
// a simple service which only counts the number of calls
class MyService {
    private var count = 0
    private val lock = ReentrantReadWriteLock()

    fun callService() {
        lock.write { Thread.sleep(10); count++ }
    }
    fun getCount(): Int = lock.read { count }
}
val service = MyService()

// an ExecutorService which configures Vaadin for every thread created.
val e: ExecutorService = Executors.newFixedThreadPool(4) { runnable ->
    Thread {
        MockVaadin.setup(routes)
        runnable.run()
        MockVaadin.tearDown()
    }
}

try {
    // submit a task to all threads, to call a service in parallel.
    repeat(4) {
        e.submit {
            try {
                UI.getCurrent().navigate("helloworld")
                _get<Button> { caption = "Hello, World!" }.onLeftClick {
                    service.callService()
                }
                _get<Button> { caption = "Hello, World!" }._click()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
} finally {
    e.shutdown()
    e.awaitTermination(10, TimeUnit.SECONDS)
}

// make sure that every thread called the service
expect(4) { service.getCount() }
```
