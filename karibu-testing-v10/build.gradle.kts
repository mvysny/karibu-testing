dependencies {
    // - don't compile-depend on vaadin-core anymore: the app itself should manage Vaadin dependencies, for example
    //   using the gradle-flow-plugin or direct dependency on vaadin-core. The reason is that the app may wish to use the
    //   npm mode and exclude all webjars.
    // - depend on "vaadin" instead of just "vaadin-core", to bring in Grid Pro.
    // - depend on the lowest Vaadin (Vaadin 14 LTS)
    compileOnly(libs.vaadin.stable.all)
    testImplementation(libs.vaadin.stable.all)

    api(libs.fake.servlet5)
    api(libs.kaributools)
    // exposed by _getPresentationJsoup(); api so consumers get the JSoup query types transitively.
    api(libs.jsoup)

    testImplementation(libs.junit.jupiter)
    api(kotlin("test"))
    testImplementation(libs.slf4j.simple)

    // to have the class autodiscovery functionality
    implementation(libs.classgraph)
}

kotlin {
    explicitApi()
}

@Suppress("UNCHECKED_CAST")
val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("karibu-testing-v10")
