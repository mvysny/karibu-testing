import java.nio.file.Files

dependencies {
    testCompile(platform("com.vaadin:vaadin-bom:${properties["vaadin14_version"]}"))
    testCompile("com.vaadin:vaadin:${properties["vaadin14_version"]}")
    testCompile(project(":karibu-testing-v10:kt10-tests"))

    // for testing out the NPM template loading from META-INF/resources/frontend/
    testCompile("com.github.appreciated:app-layout-addon:4.0.0.beta5")
}
