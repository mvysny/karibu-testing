plugins {
    groovy
}

dependencies {
    api("org.codehaus.groovy:groovy:3.0.5")
    // IDEA language injections
    api("com.intellij:annotations:12.0")

    // don't compile-depend on vaadin-core anymore: the app itself should manage Vaadin dependencies, for example
    // using the gradle-flow-plugin or direct dependency on vaadin-core. The reason is that the app may wish to use the
    // npm mode and exclude all webjars.
    compileOnly("com.vaadin:vaadin-core:${properties["vaadin19_version"]}")
    testImplementation("com.vaadin:vaadin-core:${properties["vaadin19_version"]}")

    api(project(":karibu-testing-v10"))

    implementation("org.junit.jupiter:junit-jupiter-api:${properties["junit5_version"]}")
    testImplementation("org.junit.jupiter:junit-jupiter:${properties["junit5_version"]}")
    api(kotlin("test"))
    testImplementation("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10-groovy")
