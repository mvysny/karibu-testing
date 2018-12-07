dependencies {
    compile(platform("com.vaadin:vaadin-bom:${ext["vaadin_platform_lts_version"]}"))
    compile("com.vaadin:vaadin-core:${ext["vaadin_platform_lts_version"]}")
    compile(project(":mock-servlet-environment"))

    testCompile("com.github.mvysny.dynatest:dynatest-engine:${ext["dynatest_version"]}")
    testCompile("org.slf4j:slf4j-simple:1.7.25")
    testCompile("com.github.mvysny.karibudsl:karibu-dsl-v10:0.5.1")

    // to have class autodiscovery functionality
    compile("io.github.classgraph:classgraph:4.6.3")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10")
