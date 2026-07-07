dependencies {
    api(project(":karibu-testing-v10"))
    compileOnly(libs.vaadin.stable.spring)
    // Spring Security is optional: only needed by apps that call MockSpringSecurity.mock().
    // Keep it compileOnly so non-security apps don't drag it in.
    compileOnly(libs.spring.security.core)

    testImplementation(libs.vaadin.stable.all)
    testImplementation(libs.spring.security.core)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.slf4j.simple)
    testImplementation(libs.jakarta.annotation.api)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

@Suppress("UNCHECKED_CAST")
val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("karibu-testing-v10-spring")
