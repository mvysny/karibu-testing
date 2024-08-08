dependencies {
    testImplementation(libs.vaadin.v24.all)
    testImplementation(project(":karibu-testing-v23:kt23-tests"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
