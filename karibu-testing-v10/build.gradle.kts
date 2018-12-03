dependencies {
    compile(platform("com.vaadin:vaadin-bom:${ext["vaadin10_version"]}"))
    compile("com.vaadin:vaadin-core:${ext["vaadin10_version"]}")
    compile(project(":mock-servlet-environment"))

    testCompile("com.github.mvysny.dynatest:dynatest-engine:${ext["dynatest_version"]}")
    testCompile("org.slf4j:slf4j-simple:1.7.25")
    testCompile("com.github.mvysny.karibudsl:karibu-dsl-v10:0.5.0")

    // to have class autodiscovery functionality
    compile("com.vaadin.external.atmosphere:atmosphere-runtime:2.4.24.vaadin1")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10")
