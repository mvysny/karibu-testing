dependencies {
    testImplementation(platform("com.vaadin:vaadin-bom:${properties["vaadin13_version"]}"))
    testImplementation("com.vaadin:vaadin:${properties["vaadin13_version"]}")
    testImplementation(project(":karibu-testing-v10:kt10-tests"))
}
