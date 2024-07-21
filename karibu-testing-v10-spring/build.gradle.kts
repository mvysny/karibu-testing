dependencies {
    api(project(":karibu-testing-v10"))
    compileOnly(libs.vaadin.v24.spring)
}

@Suppress("UNCHECKED_CAST")
val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("karibu-testing-v10-spring")
