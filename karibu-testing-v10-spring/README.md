# Karibu-Testing Spring Integration

Karibu-Testing offers basic support for Spring. Please see [t-shirt shop example](https://github.com/mvysny/t-shirt-shop-example) for
an example on how to use Karibu-Testing with a Spring app.

Please see [vaadin-spring-karibu-testing](https://github.com/mvysny/vaadin-spring-karibu-testing)
on an example for Spring+Karibu-Testing example project.

## Adding to your project

Add the following dependencies to your project's `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.github.mvysny.kaributesting</groupId>
        <artifactId>karibu-testing-v10-spring</artifactId>
        <version>x.y.z</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.github.mvysny.kaributesting</groupId>
        <artifactId>karibu-testing-v23</artifactId>
        <version>x.y.z</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

The first dependency will add the Karibu-Testing core jar (`karibu-testing-v10`) and a Spring integration
jar on top of that (`karibu-testing-v10-spring`); the second dependency
will add additional support for testing Vaadin 23 components such as `VirtualList`
and `MultiselectComboBox`.

## Spring Security

Spring Security is not supported out-of-the-box: Spring Security usually uses Servlet Filter
which requires Servlet Container to be up and running, yet Karibu-Testing doesn't
start any Servlet Container. See [Issue #47](https://github.com/mvysny/karibu-testing/issues/47)
for more details. One of the workarounds is to [Manually Authenticate User with Spring Security](https://www.baeldung.com/manually-set-user-authentication-spring-security),
before navigating to a view.

There are ways to enable Spring Security in Karibu's faked environment though, please
take a look at [issue #94](https://github.com/mvysny/karibu-testing/issues/94) and
[issue #180](https://github.com/mvysny/karibu-testing/issues/180). Namely, users
reported the following snippet to work - must be called before `MockVaadin.setup()`:
```kotlin
MockVaadin.mockRequestFactory = {
    object : FakeRequest(it) {
        override fun getUserPrincipal() = SecurityContextHolder.getContext().authentication
    }
}
```
