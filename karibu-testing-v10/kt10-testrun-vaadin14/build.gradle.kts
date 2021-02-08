dependencies {
    testImplementation(project(":karibu-testing-v10:kt10-tests"))
    testImplementation("com.vaadin:flow-lit-template:2.5.0.alpha2")

    // for testing out the NPM template loading from META-INF/resources/frontend/
    testImplementation("com.github.appreciated:app-layout-addon:4.0.0.beta5") {
        exclude(group = "com.vaadin")
    }
}
