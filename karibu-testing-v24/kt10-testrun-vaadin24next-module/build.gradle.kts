dependencies {
    testImplementation(libs.vaadin.v24next.all)
    testImplementation(libs.vaadin.v24next.spring)
    testImplementation(project(":karibu-testing-v23:kt23-tests")) {
        exclude(group = "com.github.appreciated")
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
