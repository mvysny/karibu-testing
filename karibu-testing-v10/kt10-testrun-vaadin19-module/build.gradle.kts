dependencies {
    testImplementation("com.vaadin:vaadin:${properties["vaadin19_version"]}")
    testImplementation(project(":karibu-testing-v10:kt10-tests")) {
        exclude(group = "com.github.appreciated")
    }
}
