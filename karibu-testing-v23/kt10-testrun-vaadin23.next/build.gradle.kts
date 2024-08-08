dependencies {
    testImplementation(libs.vaadin.v23next.all)
    testImplementation(project(":karibu-testing-v23:kt23-tests"))

    // for testing out the NPM template loading from META-INF/resources/frontend/
    testImplementation(libs.addon.applayout)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Vaadin 23 requires Java 11 or higher
tasks.test { onlyIf { JavaVersion.current() >= JavaVersion.VERSION_11 } }
