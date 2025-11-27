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
    compileOnly(libs.vaadin.v24.core)
    testImplementation(libs.vaadin.v24.core)

    api(project(":karibu-testing-v10"))

    implementation(libs.junit.jupiterapi)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    api(kotlin("test"))
    testImplementation(libs.slf4j.simple)

    // needs to be here otherwise Groovy compiler crashes
    testImplementation("org.springframework.data:spring-data-commons:4.0.0")
}

@Suppress("UNCHECKED_CAST")
val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("karibu-testing-v10-groovy")
