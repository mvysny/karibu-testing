dependencies {
    testImplementation(libs.vaadin.v14next)
    testImplementation(project(":karibu-testing-v10:kt10-tests")) {
        exclude(group = "com.vaadin")
        exclude(group = "com.github.appreciated")
    }

    testImplementation(libs.vaadin.spring)
}
