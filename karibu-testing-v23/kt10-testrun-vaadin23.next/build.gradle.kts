dependencies {
    testImplementation("com.vaadin:vaadin:${properties["vaadin23_next_version"]}")
    testImplementation(project(":karibu-testing-v23:kt23-tests"))
}

// Vaadin 23 requires Java 11 or higher
tasks.test { onlyIf { JavaVersion.current() >= JavaVersion.VERSION_11 } }
