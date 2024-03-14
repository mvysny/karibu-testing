dependencies {
    // - don't compile-depend on vaadin-core anymore: the app itself should manage Vaadin dependencies, for example
    //   using the gradle-flow-plugin or direct dependency on vaadin-core. The reason is that the app may wish to use the
    //   npm mode and exclude all webjars.
    // - depend on "vaadin" instead of just "vaadin-core", to bring in Grid Pro.
    // - depend on the lowest Vaadin (Vaadin 14 LTS)
    compileOnly(libs.vaadin.v14)
    testImplementation(libs.vaadin.v14)

    api(libs.fake.servlet)
    api(libs.karibu.tools)

    testImplementation(libs.dynatest)
    api(kotlin("test"))
    testImplementation(libs.slf4j.simple)

    // to have the class autodiscovery functionality
    implementation(libs.classgraph)
}

kotlin {
    explicitApi()
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10")
