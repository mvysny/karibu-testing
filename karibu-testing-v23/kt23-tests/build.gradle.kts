dependencies {
    api(project(":karibu-testing-v10:kt10-tests")) {
        exclude(group = "com.vaadin")
    }
    api(libs.karibudsl23) {
        exclude(module = "javax.el")
    }

    api(project(":karibu-testing-v23"))
    api("com.vaadin:vaadin-spring:${properties["vaadin24_version_spring"]}")
    compileOnly("com.vaadin:vaadin:${properties["vaadin24_version"]}")
}
