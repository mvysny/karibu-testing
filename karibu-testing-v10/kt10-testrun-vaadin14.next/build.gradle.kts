dependencies {
    testImplementation("com.vaadin:vaadin:${properties["vaadin14_next_version"]}")
    testImplementation(project(":karibu-testing-v10:kt10-tests")) {
        exclude(group = "com.vaadin")
    }

    // for testing out the NPM template loading from META-INF/resources/frontend/
    testImplementation("com.github.appreciated:app-layout-addon:4.0.0.beta5") {
        exclude(group = "com.vaadin")
    }

    testImplementation("com.vaadin:vaadin-spring:${properties["vaadin_spring_version"]}")
}
