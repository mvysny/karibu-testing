dependencies {
    testImplementation("com.vaadin:vaadin:${properties["vaadin23_version"]}")
    testImplementation(project(":karibu-testing-v23:kt23-tests"))
}
