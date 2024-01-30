dependencies {
    api("jakarta.servlet:jakarta.servlet-api:5.0.0")
    api(kotlin("stdlib-jdk8"))
    implementation("org.slf4j:slf4j-api:${properties["slf4j_version"]}")

    testImplementation(libs.dynatest)
    testImplementation("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
}

kotlin {
    explicitApi()
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("mock-servlet-environment5")
