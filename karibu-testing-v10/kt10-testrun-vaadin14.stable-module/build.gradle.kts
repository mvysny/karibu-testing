dependencies {
    testImplementation("com.vaadin:vaadin:${properties["vaadin14_version"]}")
    testImplementation(project(":karibu-testing-v10:kt10-tests")) {
        exclude(group = "com.vaadin")
        exclude(group = "com.github.appreciated")
    }

    testImplementation("com.vaadin:vaadin-spring:${properties["vaadin_spring_version"]}")
}
