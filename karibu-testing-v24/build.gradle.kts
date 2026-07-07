dependencies {
    compileOnly(libs.vaadin.stable.all)
    testImplementation(libs.vaadin.stable.all)
    api(project(":karibu-testing-v23"))

    testImplementation(libs.slf4j.simple)
}

kotlin {
    explicitApi()
}

@Suppress("UNCHECKED_CAST")
val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("karibu-testing-v24")
