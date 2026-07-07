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

### OAuth / OpenID Connect

An app secured with OAuth2 / OpenID Connect (Keycloak, Google, …) logs the user in via an
*external* redirect to the identity provider. That flow needs a real browser and a running IdP,
so it can't run browser-free; a servlet filter (`OAuth2LoginAuthenticationFilter`) performs it in
production, before Vaadin ever runs, and Karibu starts no filter. In a Karibu test you therefore
**skip the redirect** and assert on the authenticated behavior directly. See
[issue #143](https://github.com/mvysny/karibu-testing/issues/143).

Keep `@PermitAll` on your views (don't relax them to `@AnonymousAllowed` just for tests). Then:

* If your views only gate on *roles/authorities*, `MockSpringSecurity.mock()` + `@WithMockUser`
  (above) is enough - `@WithMockUser` yields a `UsernamePasswordAuthenticationToken`, whose
  authorities drive `@RolesAllowed` / `@PermitAll` exactly like the real token would.
* If your view reads *OIDC claims* off the principal (e.g. `authentication.principal as OidcUser`
  to get the email or a custom claim), `@WithMockUser` won't carry them. Populate an
  `OAuth2AuthenticationToken` in the `SecurityContextHolder` yourself before navigating -
  `MockSpringSecurity.mock()` reads whatever `Authentication` is present, including an OAuth2 one:

```kotlin
@BeforeEach fun setup() {
    MockSpringSecurity.mock()
    MockVaadin.setup(routes, ctx) { MyUI() }
}

@Test fun oidcUserCanReachDashboard() {
    val oidcUser = DefaultOidcUser(
        listOf(SimpleGrantedAuthority("ROLE_USER")),
        OidcIdToken.withTokenValue("fake-token")
            .claim("sub", "1234").claim("email", "user@acme.com").build()
    )
    SecurityContextHolder.getContext().authentication =
        OAuth2AuthenticationToken(oidcUser, oidcUser.authorities, "keycloak")

    UI.getCurrent().navigate("dashboard")
    _expectOne<DashboardView>()
}
```

`DefaultOidcUser` / `OAuth2AuthenticationToken` come from `spring-security-oauth2-client`, which
your app already depends on. Spring Security Test also ships an `oidcLogin()` request
post-processor, but that targets `MockMvc`/`WebTestClient`; under Karibu, populate the
`SecurityContextHolder` directly as shown above.
