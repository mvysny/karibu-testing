dependencies {
    compileOnly(libs.vaadin24)
    testImplementation(libs.vaadin24)
    api(project(":karibu-testing-v23"))

    testImplementation(libs.dynatest)
    testImplementation(libs.slf4j.simple)
}

kotlin {
    explicitApi()
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v24")
