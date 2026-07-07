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

You can, however, bridge Spring Security into Karibu's faked environment. Since KT 2.7.2,
`MockSpringSecurity.mock()` installs a fake request that sources both the user principal and
its roles from Spring Security's `SecurityContextHolder` - the job usually done by Spring's
servlet filter (`SecurityContextHolderAwareRequestWrapper`), which isn't triggered in the faked
environment. Call it **before** `MockVaadin.setup()`, ideally from `@BeforeEach`:

```kotlin
@BeforeEach fun setup() {
    MockSpringSecurity.mock()   // optionally MockSpringSecurity.mock(rolePrefix = "ROLE_")
    MockVaadin.setup(routes, ctx) { MyUI() }
}
```
Java:
```java
@BeforeEach void setup() {
    MockSpringSecurity.mock();
    MockVaadin.setup(routes, ctx, MyUI::new);
}
```

Now Vaadin's route security (`@RolesAllowed`, `@PermitAll`, `NavigationAccessControl`, …) sees the
currently logged-in Spring Security user. The `rolePrefix` (default `ROLE_`, mirroring Spring's
`GrantedAuthorityDefaults`) is prepended to the role before it's matched against the user's
authorities: `@RolesAllowed("ADMIN")` matches the authority `ROLE_ADMIN`. Pass `rolePrefix = ""`
if your app gates on raw authorities.

`MockSpringSecurity.mock()` **doesn't** log in the user - it only carries an already-established
security context over to the faked request. You still log the user in via Spring Security Test's
`@WithMockUser` / `@WithUserDetails` (which populate `SecurityContextHolder` for you) or manually:

```kotlin
@WithMockUser(username = "admin", roles = ["ADMIN"])
@Test fun adminCanReachAdminView() {
    UI.getCurrent().navigate("admin")
    _expectOne<AdminView>()
}
```

* The [vaadin-spring-karibu-testing](https://github.com/mvysny/vaadin-spring-karibu-testing)
  example app which demoes the Spring Security as well
* [Browserless Testing of Vaadin Apps With Karibu Testing](https://martinelli.ch/browserless-testing-of-vaadin-applications-with-karibu-testing/)
  by Simon Martinelli.
