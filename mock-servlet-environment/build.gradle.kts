dependencies {
    compile("javax.servlet:javax.servlet-api:3.1.0")
    compile(kotlin("stdlib-jdk8"))
    compile("org.slf4j:slf4j-api:${properties["slf4j_version"]}")

    testCompile("com.github.mvysny.dynatest:dynatest-engine:${properties["dynatest_version"]}")
    testCompile("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("mock-servlet-environment")
