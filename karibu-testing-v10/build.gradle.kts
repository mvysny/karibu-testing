dependencies {
    // don't compile-depend on vaadin-core anymore: the app itself should manage Vaadin dependencies, for example
    // using the gradle-flow-plugin or direct dependency on vaadin-core. The reason is that the app may wish to use the
    // npm mode and exclude all webjars.
    compileOnly(platform("com.vaadin:vaadin-bom:${properties["vaadin14_version"]}"))
    compileOnly("com.vaadin:vaadin-core:${properties["vaadin14_version"]}")
    testCompile("com.vaadin:vaadin-core:${properties["vaadin14_version"]}")

    compile(project(":mock-servlet-environment"))

    testCompile("com.github.mvysny.dynatest:dynatest-engine:${properties["dynatest_version"]}")
    compile(kotlin("test"))
    testCompile("org.slf4j:slf4j-simple:1.7.25")

    // to have class autodiscovery functionality
    compile("io.github.classgraph:classgraph:4.6.23")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10")
