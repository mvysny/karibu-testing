dependencies {
    testImplementation(platform("com.vaadin:vaadin-bom:${properties["vaadin14_version"]}"))
    testImplementation("com.vaadin:vaadin:${properties["vaadin14_version"]}")
    testImplementation(project(":karibu-testing-v10:kt10-tests"))
}
