import com.jfrog.bintray.gradle.BintrayExtension
import groovy.lang.Closure
import org.gradle.api.tasks.GradleBuild
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    kotlin("jvm") version "1.3.0"
    id("com.jfrog.bintray") version "1.8.1"
    `maven-publish`
}

defaultTasks("clean", "build")

allprojects {
    group = "com.github.kaributesting"
    version = "0.5.6-SNAPSHOT"
    repositories {
        jcenter()
        maven { setUrl("https://dl.bintray.com/mvysny/github") }
    }
}

subprojects {
    ext["vaadin8_version"] = "8.6.0"
    ext["vaadin10_version"] = "11.0.1"
    ext["dynatest_version"] = "0.11"

    apply {
        plugin("maven-publish")
        plugin("kotlin")
        plugin("com.jfrog.bintray")
    }

    // creates a reusable function which configures proper deployment to Bintray
    ext["configureBintray"] = { artifactId: String ->

        val local = Properties()
        val localProperties: File = rootProject.file("local.properties")
        if (localProperties.exists()) {
            localProperties.inputStream().use { local.load(it) }
        }

        val java: JavaPluginConvention = convention.getPluginByName("java")

        val sourceJar = task("sourceJar", Jar::class) {
            dependsOn(tasks.findByName("classes"))
            classifier = "sources"
            from(java.sourceSets["main"].allSource)
        }

        publishing {
            publications {
                create("mavenJava", MavenPublication::class.java).apply {
                    groupId = project.group.toString()
                    this.artifactId = artifactId
                    version = project.version.toString()
                    pom.withXml {
                        val root = asNode()
                        root.appendNode("description", "Karibu Testing, support for browserless Vaadin testing in Kotlin")
                        root.appendNode("name", artifactId)
                        root.appendNode("url", "https://github.com/mvysny/karibu-testing")
                    }
                    from(components.findByName("java")!!)
                    artifact(sourceJar) {
                        classifier = "sources"
                    }
                }
            }
        }

        bintray {
            user = local.getProperty("bintray.user")
            key = local.getProperty("bintray.key")
            pkg(closureOf<BintrayExtension.PackageConfig> {
                repo = "github"
                name = "com.github.kaributesting"
                setLicenses("Apache-2.0")
                vcsUrl = "https://github.com/mvysny/karibu-testing"
                publish = true
                setPublications("mavenJava")
                version(closureOf<BintrayExtension.VersionConfig> {
                    this.name = project.version.toString()
                    released = Date().toString()
                })
            })
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            // to see the exceptions of failed tests in Travis-CI console.
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}

