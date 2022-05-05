dependencies {
    testImplementation("com.vaadin:vaadin:${properties["vaadin23_next_version"]}")
    testImplementation(project(":karibu-testing-v10:kt19-tests")) {
        exclude(group = "com.github.appreciated")
    }
}

// Vaadin 23 requires Java 11 or higher
tasks.test { onlyIf { JavaVersion.current() >= JavaVersion.VERSION_11 } }
