dependencies {
    testImplementation(libs.vaadin.v14.all)
    testImplementation(project(":karibu-testing-v10:kt10-tests")) {
        exclude(group = "com.vaadin")
        exclude(group = "com.github.appreciated")
    }

    testImplementation(libs.vaadin.spring)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
