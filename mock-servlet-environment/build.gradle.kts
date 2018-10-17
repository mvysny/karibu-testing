dependencies {
    compile("javax.servlet:javax.servlet-api:3.1.0")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compile("org.slf4j:slf4j-api:1.7.25")

    testCompile("com.github.mvysny.dynatest:dynatest-engine:${ext["dynatest_version"]}")
    testCompile("org.slf4j:slf4j-simple:1.7.25")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("mock-servlet-environment")
