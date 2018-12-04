repositories {
    maven { setUrl("https://maven.vaadin.com/vaadin-prereleases") }
}

dependencies {
    compile(platform("com.vaadin:vaadin-bom:${ext["vaadin12_version"]}"))
    compile("com.vaadin:vaadin-core:${ext["vaadin12_version"]}")
    compile(project(":karibu-testing-v10:kt10-tests"))
}
