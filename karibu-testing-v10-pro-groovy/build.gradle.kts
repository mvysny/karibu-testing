plugins {
    groovy
}

dependencies {
    api(project(":karibu-testing-v10-groovy"))

    // 1. don't compile-depend on vaadin-core anymore: the app itself should manage Vaadin dependencies, for example
    // using the gradle-flow-plugin or direct dependency on vaadin-core. The reason is that the app may wish to use the
    // npm mode and exclude all webjars.
    // depend on vaadin instead of vaadin-core, to bring in Confirm Dialog and Grid Pro.
    compileOnly(libs.vaadin.v24.core)
    testImplementation(libs.vaadin.v24.core)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.slf4j.simple)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

@Suppress("UNCHECKED_CAST")
val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("karibu-testing-v10-pro-groovy")
