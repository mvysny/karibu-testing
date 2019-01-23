import com.jfrog.bintray.gradle.BintrayExtension
import groovy.lang.Closure
import org.gradle.api.tasks.GradleBuild
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    kotlin("jvm") version "1.3.20"
    id("com.jfrog.bintray") version "1.8.3"
    `maven-publish`
    id("org.jetbrains.dokka") version "0.9.17"
}

defaultTasks("clean", "build")

allprojects {
    group = "com.github.mvysny.kaributesting"
    version = "1.0.7-SNAPSHOT"
    repositories {
        jcenter()
    }
}

subprojects {
    ext["vaadin8_version"] = "8.6.4"
    ext["vaadin11_version"] = "11.0.4"
    ext["vaadin12_version"] = "12.0.4"
    ext["dynatest_version"] = "0.13"
    // here we should depend on latest Vaadin LTS version
    ext["vaadin_platform_lts_version"] = ext["vaadin11_version"]

    apply {
        plugin("maven-publish")
        plugin("kotlin")
        plugin("com.jfrog.bintray")
        plugin("org.jetbrains.dokka")
    }

    // creates a reusable function which configures proper deployment to Bintray
    ext["configureBintray"] = { artifactId: String ->

        val local = Properties()
        val localProperties: File = rootProject.file("local.properties")
        if (localProperties.exists()) {
            localProperties.inputStream().use { local.load(it) }
        }

        val sourceJar = task("sourceJar", Jar::class) {
            dependsOn(tasks["classes"])
            classifier = "sources"
            from(sourceSets.main.get().allSource)
        }

        val javadocJar = task("javadocJar", Jar::class) {
            val javadoc = tasks["dokka"] as DokkaTask
            javadoc.outputFormat = "javadoc"
            javadoc.outputDirectory = "$buildDir/javadoc"
            dependsOn(javadoc)
            classifier = "javadoc"
            from(javadoc.outputDirectory)
        }

        publishing {
            publications {
                create("mavenJava", MavenPublication::class.java).apply {
                    groupId = project.group.toString()
                    this.artifactId = artifactId
                    version = project.version.toString()
                    pom {
                        description.set("Karibu Testing, support for browserless Vaadin testing in Kotlin")
                        name.set(artifactId)
                        url.set("https://github.com/mvysny/karibu-testing")
                        licenses {
                            license {
                                name.set("The Apache Software License, Version 2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                                distribution.set("repo")
                            }
                        }
                        developers {
                            developer {
                                id.set("mavi")
                                name.set("Martin Vysny")
                                email.set("martin@vysny.me")
                            }
                        }
                        scm {
                            url.set("https://github.com/mvysny/karibu-testing")
                        }
                    }

                    from(components["java"])
                    artifact(sourceJar)
                    artifact(javadocJar)
                }
            }
        }

        bintray {
            user = local.getProperty("bintray.user")
            key = local.getProperty("bintray.key")
            pkg(closureOf<BintrayExtension.PackageConfig> {
                repo = "github"
                name = "com.github.mvysny.kaributesting"
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

