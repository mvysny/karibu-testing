dependencies {
    testImplementation("com.vaadin:vaadin:${properties["vaadin23_version"]}")
    testImplementation(project(":karibu-testing-v10:kt19-tests")) {
        exclude(group = "com.github.appreciated")
    }
}
