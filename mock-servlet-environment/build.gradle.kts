dependencies {
    api("javax.servlet:javax.servlet-api:3.1.0")
    api(kotlin("stdlib-jdk8"))
    api("org.slf4j:slf4j-api:${properties["slf4j_version"]}")

    testImplementation("com.github.mvysny.dynatest:dynatest-engine:${properties["dynatest_version"]}")
    testImplementation("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("mock-servlet-environment")
