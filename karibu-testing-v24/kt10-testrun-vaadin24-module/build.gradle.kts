dependencies {
    testImplementation(libs.vaadin.v24.all)
    testImplementation(project(":karibu-testing-v23:kt23-tests")) {
        exclude(group = "com.github.appreciated")
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
