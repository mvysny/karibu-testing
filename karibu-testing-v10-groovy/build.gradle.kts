plugins {
    groovy
}

dependencies {
    api("org.codehaus.groovy:groovy:3.0.17")
    // IDEA language injections
    api("org.jetbrains:annotations:24.0.1")

    // 1. don't compile-depend on vaadin-core anymore: the app itself should manage Vaadin dependencies, for example
    // using the gradle-flow-plugin or direct dependency on vaadin-core. The reason is that the app may wish to use the
    // npm mode and exclude all webjars.
    compileOnly(libs.vaadincore24)
    testImplementation(libs.vaadincore24)

    api(project(":karibu-testing-v10"))

    implementation(libs.junit.jupiterapi)
    testImplementation(libs.junit.jupiter)
    api(kotlin("test"))
    testImplementation(libs.slf4j.simple)
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10-groovy")
