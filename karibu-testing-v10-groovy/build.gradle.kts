plugins {
    groovy
}

dependencies {
    api(libs.groovy)
    // IDEA language injections
    api(libs.jetbrains.annotations)

    // 1. don't compile-depend on vaadin-core anymore: the app itself should manage Vaadin dependencies, for example
    // using the gradle-flow-plugin or direct dependency on vaadin-core. The reason is that the app may wish to use the
    // npm mode and exclude all webjars.
    // 2. Don't depend on Vaadin 23: it requires Java11+ and Groovy fails for some reason.
    compileOnly(libs.vaadin.v14.core)
    testImplementation(libs.vaadin.v14.core)

    api(project(":karibu-testing-v10"))

    implementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.runtime)
    api(kotlin("test"))
    testImplementation(libs.slf4j.simple)
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10-groovy")
