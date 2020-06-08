dependencies {
    testImplementation("com.vaadin:vaadin:${properties["vaadin16_version"]}")
    testImplementation(project(":karibu-testing-v10:kt10-tests"))

    // for testing out the NPM template loading from META-INF/resources/frontend/
    testImplementation("com.github.appreciated:app-layout-addon:4.0.0.beta5")
}
