dependencies {
    compile(platform("com.vaadin:vaadin-bom:${ext["vaadin11_version"]}"))
    compile("com.vaadin:vaadin-core:${ext["vaadin11_version"]}")
    compile(project(":karibu-testing-v10:kt10-tests"))
}
