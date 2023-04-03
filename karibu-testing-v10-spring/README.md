Karibu-Testing doesn't officially support Spring. However, there are couple of very
simple example projects that might help you get started.

Please see [t-shirt shop example](https://github.com/mvysny/t-shirt-shop-example) on
an example on how to use Karibu-Testing with a Spring app.

Note that Spring Security is not supported: Spring Security uses Servlet Filter
which requires Servlet Container to be up and running, yet Karibu-Testing doesn't
start any Servlet Container. See [Issue #47](https://github.com/mvysny/karibu-testing/issues/47)
for more details.

## Adding to your project

Add the following dependencies to your project's `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.github.mvysny.kaributesting</groupId>
        <artifactId>karibu-testing-v10-spring</artifactId>
        <version>1.3.23</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.github.mvysny.kaributesting</groupId>
        <artifactId>karibu-testing-v23</artifactId>
        <version>1.3.23</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

The first dependency will add the Karibu-Testing core jar (`karibu-testing-v10`) and a Spring integration
jar on top of that (`karibu-testing-v10-spring`); the second dependency
will add additional support for testing Vaadin 23 components such as `VirtualList`
and `MultiselectComboBox`.
