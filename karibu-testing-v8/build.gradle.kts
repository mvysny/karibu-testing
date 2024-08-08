dependencies {
    api(libs.vaadin8.server)
    api(libs.fake.servlet)
    implementation(libs.slf4j.api)

    // these are needed to remove warnings during javadoc gradle task
    compileOnly(libs.javax.portletapi)
    compileOnly(libs.javax.validation)
    compileOnly(libs.atmosphere)
    compileOnly(libs.gwt.user)

    testImplementation(libs.dynatest)
    api(kotlin("test"))
    testImplementation(libs.slf4j.simple)
    testImplementation(libs.karibu.dsl8)
    testImplementation(libs.vaadin8.client.compiled)
}

kotlin {
    explicitApi()
}

@Suppress("UNCHECKED_CAST")
val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("karibu-testing-v8")
