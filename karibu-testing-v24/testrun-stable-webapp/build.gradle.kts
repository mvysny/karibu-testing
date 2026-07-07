dependencies {
    testImplementation(libs.vaadin.stable.all)
    testImplementation(project(":karibu-testing-v23:tests"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
