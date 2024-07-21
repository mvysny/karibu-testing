dependencies {
    api(project(":karibu-testing-v10"))
    compileOnly(libs.vaadin.v24.spring)
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10-spring")
