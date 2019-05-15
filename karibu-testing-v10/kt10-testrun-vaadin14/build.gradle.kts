repositories {
    jcenter()
    maven { setUrl("https://maven.vaadin.com/vaadin-prereleases/") }
    maven { setUrl("https://oss.sonatype.org/content/repositories/vaadin-snapshots/") }
}

dependencies {
    compile(platform("com.vaadin:vaadin-bom:${properties["vaadin14_version"]}"))
    compile("com.vaadin:vaadin-core:${properties["vaadin14_version"]}")
    compile(project(":karibu-testing-v10:kt10-tests"))
}
