[![GitHub tag](https://img.shields.io/github/tag/mvysny/karibu-testing.svg)](https://github.com/mvysny/karibu-testing/tags)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.mvysny.kaributesting/mock-servlet-environment/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.mvysny.kaributesting/mock-servlet-environment)

# A mock/fake servlet 5 API implementation

This project implements all of the jakarta.servlet interfaces, allowing you to create a
mock servlet environment quickly.

To start, just add the following lines into your Gradle `build.gradle` file:

```groovy
repositories {
    mavenCentral()
}
dependencies {
    testImplementation("com.github.mvysny.kaributesting:mock-servlet-environment5:x.y.z")
}
```

> Note: obtain the newest version from the tag name above

For Maven it's really easy: the jar is published on Maven Central, so all you need to do is to add the dependency
to your `pom.xml`:

```xml
<project>
	<dependencies>
		<dependency>
			<groupId>com.github.mvysny.kaributesting</groupId>
			<artifactId>mock-servlet-environment5</artifactId>
			<version>x.y.z</version>
			<scope>test</scope>
		</dependency>
    </dependencies>
</project>
```

## Quickstart

Use the following code to obtain the mock/fake instances:

```kotlin
val context: ServletContext = MockContext()
val servletConfig: ServletConfig = MockServletConfig(context)
val session: HttpSession = MockHttpSession.create(context)
val request: HttpServletRequest = MockRequest(session)
val response: HttpServletResponse = MockResponse()
```

See the `MockHttpEnvironment` class to tune the values returned by the `MockRequest` class.
