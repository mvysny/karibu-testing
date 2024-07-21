dependencies {
    // - don't compile-depend on vaadin-core anymore: the app itself should manage Vaadin dependencies, for example
    //   using the gradle-flow-plugin or direct dependency on vaadin-core. The reason is that the app may wish to use the
    //   npm mode and exclude all webjars.
    // - depend on "vaadin" instead of just "vaadin-core", to bring in Grid Pro.
    // - depend on the latest Vaadin (Vaadin 23 LTS)
    compileOnly(libs.vaadin.v23.all)
    testImplementation(libs.vaadin.v23.all)
    api(project(":karibu-testing-v10"))
    api(libs.karibu.tools23)

    testImplementation(libs.dynatest)
    testImplementation(libs.slf4j.simple)
}

kotlin {
    explicitApi()
}

@Suppress("UNCHECKED_CAST")
val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("karibu-testing-v23")
