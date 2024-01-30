dependencies {
    api(project(":karibu-testing-v10"))
    compileOnly(libs.vaadinspring24)
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10-spring")
