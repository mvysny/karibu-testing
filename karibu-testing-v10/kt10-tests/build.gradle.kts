dependencies {
    compile(project(":karibu-testing-v10"))

    // for testing purposes
    compile("com.github.mvysny.dynatest:dynatest-engine:${properties["dynatest_version"]}")
    compile("org.slf4j:slf4j-simple:1.7.25")
    compile("com.github.mvysny.karibudsl:karibu-dsl-v10:${properties["karibudsl_version"]}")
    compileOnly("com.vaadin:vaadin:${properties["vaadin14_version"]}")
}
