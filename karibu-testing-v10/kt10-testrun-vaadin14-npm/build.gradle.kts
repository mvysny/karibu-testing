import java.nio.file.Files

dependencies {
    testCompile(platform("com.vaadin:vaadin-bom:${properties["vaadin14_version"]}"))
    testCompile("com.vaadin:vaadin-core:${properties["vaadin14_version"]}")
    testCompile(project(":karibu-testing-v10:kt10-tests"))

    // for testing out the NPM template loading from META-INF/resources/frontend/
    testCompile("com.github.appreciated:app-layout-addon:4.0.0.beta5")
}

tasks.named<Task>("test") { doFirst {
    val targetResourcesDir: File = sourceSets.main.get().output.resourcesDir!!
    val vaadinConfigDir: File = File(targetResourcesDir, "META-INF/VAADIN/config").apply {
        Files.createDirectories(toPath())
    }
    File(vaadinConfigDir, "flow-build-info.json").writeText("""
        {
          "compatibilityMode": false,
          "productionMode": false,
          "npmFolder": "${project.projectDir}",
          "generatedFolder": "${project.projectDir}/target/frontend",
          "frontendFolder": "${project.projectDir}/frontend"
        }
    """.trimIndent())
}}
