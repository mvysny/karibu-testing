dependencies {
    compile("com.vaadin:vaadin-server:${properties["vaadin8_version"]}")
    compile(project(":mock-servlet-environment"))

    testCompile("com.github.mvysny.dynatest:dynatest-engine:${properties["dynatest_version"]}")
    compile(kotlin("test"))
    testCompile("org.slf4j:slf4j-simple:1.7.25")
    testCompile("com.github.mvysny.karibudsl:karibu-dsl-v8:${properties["karibudsl_version"]}")
    testCompile("com.vaadin:vaadin-client-compiled:${properties["vaadin8_version"]}")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v8")
