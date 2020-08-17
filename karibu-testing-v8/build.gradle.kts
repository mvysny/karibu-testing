dependencies {
    api("com.vaadin:vaadin-server:${properties["vaadin8_version"]}")
    api(project(":mock-servlet-environment"))
    implementation("org.slf4j:slf4j-api:${properties["slf4j_version"]}")

    testImplementation("com.github.mvysny.dynatest:dynatest-engine:${properties["dynatest_version"]}")
    api(kotlin("test"))
    testImplementation("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
    testImplementation("com.github.mvysny.karibudsl:karibu-dsl-v8:${properties["karibudsl_version"]}")
    testImplementation("com.vaadin:vaadin-client-compiled:${properties["vaadin8_version"]}")
}

kotlin {
    explicitApi()
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v8")
