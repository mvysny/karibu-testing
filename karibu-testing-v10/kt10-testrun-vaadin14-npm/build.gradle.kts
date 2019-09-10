import java.nio.file.Files

dependencies {
    compile(platform("com.vaadin:vaadin-bom:${properties["vaadin14_version"]}"))
    compile("com.vaadin:vaadin-core:${properties["vaadin14_version"]}")
    compile(project(":karibu-testing-v10:kt10-tests"))
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
