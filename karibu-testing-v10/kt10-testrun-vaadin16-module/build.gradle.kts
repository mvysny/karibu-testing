dependencies {
    testImplementation("com.vaadin:vaadin:${properties["vaadin16_version"]}")
    testImplementation(project(":karibu-testing-v10:kt10-tests"))
}
