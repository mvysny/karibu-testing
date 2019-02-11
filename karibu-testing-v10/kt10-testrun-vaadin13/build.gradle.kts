dependencies {
    compile(platform("com.vaadin:vaadin-bom:${properties["vaadin13_version"]}"))
    compile("com.vaadin:vaadin-core:${properties["vaadin13_version"]}")
    compile(project(":karibu-testing-v10:kt10-tests"))
}
