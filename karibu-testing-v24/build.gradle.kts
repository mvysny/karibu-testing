dependencies {
    compileOnly("com.vaadin:vaadin:${properties["vaadin24_version"]}")
    testImplementation("com.vaadin:vaadin:${properties["vaadin24_version"]}")
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
