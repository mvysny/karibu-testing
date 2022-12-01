dependencies {
    api(project(":karibu-testing-v10"))
    compileOnly("com.vaadin:vaadin-spring:${properties["vaadin_spring_version"]}")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10-spring")
