dependencies {
    testImplementation(libs.vaadin.next.all)
    testImplementation(libs.vaadin.next.spring)
    testImplementation(project(":karibu-testing-v23:kt23-tests"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
