dependencies {
    compile(platform("com.vaadin:vaadin-bom:${properties["vaadin_platform_lts_version"]}"))
    compile("com.vaadin:vaadin-core:${properties["vaadin_platform_lts_version"]}")
    // accidentally missing deps:
    compile("com.vaadin:vaadin-select-flow:1.0.0")

    compile(project(":mock-servlet-environment"))

    testCompile("com.github.mvysny.dynatest:dynatest-engine:${properties["dynatest_version"]}")
    compile(kotlin("test"))
    testCompile("org.slf4j:slf4j-simple:1.7.25")
    testCompile("com.github.mvysny.karibudsl:karibu-dsl-v10:0.5.3")

    // to have class autodiscovery functionality
    compile("io.github.classgraph:classgraph:4.6.23")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10")
