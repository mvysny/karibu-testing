dependencies {
    compile(project(":karibu-testing-v10"))

    // for testing purposes
    compile("com.github.mvysny.dynatest:dynatest-engine:${ext["dynatest_version"]}")
    compile("org.slf4j:slf4j-simple:1.7.25")
    compile("com.github.mvysny.karibudsl:karibu-dsl-v10:0.5.1")
}
