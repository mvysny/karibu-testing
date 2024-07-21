dependencies {
    compileOnly(libs.vaadin.v24.all)
    testImplementation(libs.vaadin.v24.all)
    api(project(":karibu-testing-v23"))

    testImplementation(libs.dynatest)
    testImplementation(libs.slf4j.simple)
}

kotlin {
    explicitApi()
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v24")
