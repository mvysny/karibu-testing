dependencies {
    testImplementation(libs.vaadin.v23next.all)
    testImplementation(project(":karibu-testing-v23:kt23-tests")) {
        exclude(group = "com.github.appreciated")
    }
}

// Vaadin 23 requires Java 11 or higher
tasks.test { onlyIf { JavaVersion.current() >= JavaVersion.VERSION_11 } }
