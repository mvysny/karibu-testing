dependencies {
    compileOnly("com.vaadin:vaadin:${properties["vaadin24_version"]}")
    testImplementation("com.vaadin:vaadin:${properties["vaadin24_version"]}")
    api(project(":karibu-testing-v23"))

    testImplementation("com.github.mvysny.dynatest:dynatest:${properties["dynatest_version"]}")
    testImplementation("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
}

kotlin {
    explicitApi()
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v24")
