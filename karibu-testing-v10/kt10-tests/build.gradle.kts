dependencies {
    api(project(":karibu-testing-v10"))

    // for testing purposes
    api("com.github.mvysny.dynatest:dynatest-engine:${properties["dynatest_version"]}")
    api("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
    api("com.github.mvysny.karibudsl:karibu-dsl-v10:${properties["karibudsl_version"]}")
    api("com.vaadin:vaadin:${properties["vaadin15_version"]}")
}
