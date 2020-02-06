dependencies {
    // depend on `vaadin` instead of `vaadin-core`, to bring in Grid Pro.
    compileOnly("com.vaadin:vaadin:${properties["vaadin15_version"]}")
    testCompile("com.vaadin:vaadin:${properties["vaadin15_version"]}")

    compile(project(":mock-servlet-environment"))

    testCompile("com.github.mvysny.dynatest:dynatest-engine:${properties["dynatest_version"]}")
    compile(kotlin("test"))
    testCompile("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")

    // to have class autodiscovery functionality
    compile("io.github.classgraph:classgraph:4.6.23")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10")
