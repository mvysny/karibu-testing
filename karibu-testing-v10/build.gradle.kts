dependencies {
    // - don't compile-depend on vaadin-core anymore: the app itself should manage Vaadin dependencies, for example
    //   using the gradle-flow-plugin or direct dependency on vaadin-core. The reason is that the app may wish to use the
    //   npm mode and exclude all webjars.
    // - depend on "vaadin" instead of just "vaadin-core", to bring in Grid Pro.
    // - depend on the lowest Vaadin (Vaadin 14 LTS)
    compileOnly("com.vaadin:vaadin:${properties["vaadin14_version"]}")
    testImplementation("com.vaadin:vaadin:${properties["vaadin14_version"]}")

    api(libs.fake.servlet)
    api("com.github.mvysny.karibu-tools:karibu-tools:${properties["karibu_tools_version"]}")

    testImplementation("com.github.mvysny.dynatest:dynatest:${properties["dynatest_version"]}")
    api(kotlin("test"))
    testImplementation("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")

    // to have the class autodiscovery functionality
    implementation("io.github.classgraph:classgraph:4.8.149")
}

kotlin {
    explicitApi()
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10")
