plugins {
    groovy
}

dependencies {
    api(project(":karibu-testing-v10-groovy"))

    // 1. don't compile-depend on vaadin-core anymore: the app itself should manage Vaadin dependencies, for example
    // using the gradle-flow-plugin or direct dependency on vaadin-core. The reason is that the app may wish to use the
    // npm mode and exclude all webjars.
    // depend on vaadin instead of vaadin-core, to bring in Confirm Dialog and Grid Pro.
    compileOnly("com.vaadin:vaadin:${properties["vaadin24_version"]}")
    testImplementation("com.vaadin:vaadin:${properties["vaadin24_version"]}")

    testImplementation("org.junit.jupiter:junit-jupiter:${properties["junit5_version"]}")
    testImplementation(libs.slf4j.simple)
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10-pro-groovy")
