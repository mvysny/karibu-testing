dependencies {
    testImplementation(libs.vaadin24)
    testImplementation(project(":karibu-testing-v23:kt23-tests")) {
        exclude(group = "com.github.appreciated")
    }
}
