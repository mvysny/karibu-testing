plugins {
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
}

dependencyManagement {
    imports { mavenBom("com.vaadin:vaadin-bom:${ext["vaadin10_version"]}") }
}

dependencies {
    compile("com.vaadin:vaadin-core:${ext["vaadin10_version"]}")
    compile(project(":mock-servlet-environment"))

    testCompile("com.github.mvysny.dynatest:dynatest-engine:${ext["dynatest_version"]}")
    testCompile("org.slf4j:slf4j-simple:1.7.25")
    testCompile("com.github.vok.karibudsl:karibu-dsl-v10:0.4.11")

    // to have class autodiscovery functionality
    compile("com.vaadin.external.atmosphere:atmosphere-runtime:2.4.24.vaadin1")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10")
