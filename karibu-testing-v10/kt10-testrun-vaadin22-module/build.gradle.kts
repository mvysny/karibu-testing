dependencies {
    testImplementation("com.vaadin:vaadin:${properties["vaadin22_version"]}")
    testImplementation(project(":karibu-testing-v23:kt23-tests")) {
        exclude(group = "com.github.appreciated")
    }
}
