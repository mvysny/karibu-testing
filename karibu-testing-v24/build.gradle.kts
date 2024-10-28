dependencies {
    compileOnly(libs.vaadin.v24.all)
    testImplementation(libs.vaadin.v24.all)
    api(project(":karibu-testing-v23"))

    testImplementation(libs.slf4j.simple)
}

kotlin {
    explicitApi()
}

@Suppress("UNCHECKED_CAST")
val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("karibu-testing-v24")
