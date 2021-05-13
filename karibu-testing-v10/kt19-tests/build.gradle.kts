dependencies {
    api(project(":karibu-testing-v10:kt10-tests")) {
        exclude(group = "com.vaadin")
    }
    api("com.vaadin:vaadin-spring:12.2.0")
    api("com.vaadin:vaadin:${properties["vaadin19_version"]}")
}
