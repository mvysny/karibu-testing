dependencies {
    api(project(":karibu-testing-v10:kt10-tests")) {
        exclude(group = "com.vaadin")
    }
    api("com.github.mvysny.karibudsl:karibu-dsl-v23:${properties["karibudsl11_version"]}") {
        exclude(module = "javax.el")
    }

    api(project(":karibu-testing-v23"))
    api("com.vaadin:vaadin-spring:${properties["vaadin24_version_spring"]}")
    compileOnly("com.vaadin:vaadin:${properties["vaadin24_version"]}")
}
