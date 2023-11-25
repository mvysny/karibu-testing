dependencies {
    api(project(":karibu-testing-v10"))
    compileOnly("com.vaadin:vaadin-spring:${properties["vaadin24_version_spring"]}")
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10-spring")
