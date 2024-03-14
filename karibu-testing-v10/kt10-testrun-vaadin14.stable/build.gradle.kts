dependencies {
    testImplementation(libs.vaadin.v14.all)
    testImplementation(project(":karibu-testing-v10:kt10-tests")) {
        exclude(group = "com.vaadin")
    }

    // for testing out the NPM template loading from META-INF/resources/frontend/
    testImplementation(libs.addon.applayout) {
        exclude(group = "com.vaadin")
    }

    testImplementation(libs.vaadin.spring)
}
