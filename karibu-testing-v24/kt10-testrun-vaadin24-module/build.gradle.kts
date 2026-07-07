dependencies {
    testImplementation(libs.vaadin.stable.all)
    testImplementation(project(":karibu-testing-v23:kt23-tests")) {
        exclude(group = "com.github.appreciated")
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
