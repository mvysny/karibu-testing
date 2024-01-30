dependencies {
    // - don't compile-depend on vaadin-core anymore: the app itself should manage Vaadin dependencies, for example
    //   using the gradle-flow-plugin or direct dependency on vaadin-core. The reason is that the app may wish to use the
    //   npm mode and exclude all webjars.
    // - depend on "vaadin" instead of just "vaadin-core", to bring in Grid Pro.
    // - depend on the lowest Vaadin (Vaadin 14 LTS)
    compileOnly(libs.vaadin24)
    testImplementation(libs.vaadin24)

    api(project(":mock-servlet-environment5"))
    api(libs.kaributools)

    testImplementation(libs.dynatest)
    api(kotlin("test"))
    testImplementation(libs.slf4j.simple)

    // to have the class autodiscovery functionality
    implementation("io.github.classgraph:classgraph:4.8.151")
}

kotlin {
    explicitApi()
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10")
