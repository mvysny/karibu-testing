dependencies {
    testImplementation("com.vaadin:vaadin:${properties["vaadin24next_version"]}")
    testImplementation("com.vaadin:vaadin-spring:${properties["vaadin24next_version_spring"]}")
    testImplementation(project(":karibu-testing-v23:kt23-tests"))
}
