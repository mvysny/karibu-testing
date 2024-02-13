dependencies {
    api(project(":karibu-testing-v10"))
    compileOnly(libs.vaadin.v24.spring)
}

@Suppress("UNCHECKED_CAST")
val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10-spring")
