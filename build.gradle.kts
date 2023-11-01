import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.20"
    `maven-publish`
    signing
}

defaultTasks("clean", "build")

if (JavaVersion.current() < JavaVersion.VERSION_17) {
    throw GradleException("Karibu-Testing 2+ requires JDK 17; current JDK is ${JavaVersion.current()}")
}

allprojects {
    group = "com.github.mvysny.kaributesting"
    version = "2.1.1-SNAPSHOT"
    repositories {
        mavenCentral()
        maven(url = "https://maven.vaadin.com/vaadin-addons")
        maven(url = "https://maven.vaadin.com/vaadin-prereleases/")
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}

subprojects {
    apply {
        plugin("maven-publish")
        plugin("kotlin")
        plugin("org.gradle.signing")
    }

    // creates a reusable function which configures proper deployment to Maven Central
    ext["configureBintray"] = { artifactId: String ->

        java {
            withJavadocJar()
            withSourcesJar()
        }

        tasks.withType<Javadoc> {
            isFailOnError = false
        }

        publishing {
            repositories {
                maven {
                    setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = project.properties["ossrhUsername"] as String? ?: "Unknown user"
                        password = project.properties["ossrhPassword"] as String? ?: "Unknown user"
                    }
                }
            }
            publications {
                create("mavenJava", MavenPublication::class.java).apply {
                    groupId = project.group.toString()
                    this.artifactId = artifactId
                    version = project.version.toString()
                    pom {
                        description = "Karibu Testing, support for browserless Vaadin testing in Kotlin"
                        name = artifactId
                        url = "https://github.com/mvysny/karibu-testing"
                        licenses {
                            license {
                                name = "The Apache Software License, Version 2.0"
                                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                                distribution = "repo"
                            }
                        }
                        developers {
                            developer {
                                id = "mavi"
                                name = "Martin Vysny"
                                email = "martin@vysny.me"
                            }
                        }
                        scm {
                            url = "https://github.com/mvysny/karibu-testing"
                        }
                    }

                    from(components["java"])
                }
            }
        }

        if (project.properties["signing.keyId"] != null) {
            signing {
                sign(publishing.publications["mavenJava"])
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            // to see the exceptions of failed tests in CI console.
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    configurations.implementation {
        exclude(module = "fusion-endpoint") // Vaadin 22: exclude fusion: it brings tons of dependencies (including swagger)

        // Webjars are only needed when running in Vaadin 13 compatibility mode
        listOf("com.vaadin.webjar", "org.webjars.bowergithub.insites",
            "org.webjars.bowergithub.polymer", "org.webjars.bowergithub.polymerelements",
            "org.webjars.bowergithub.vaadin", "org.webjars.bowergithub.webcomponents")
            .forEach { exclude(group = it) }
    }
}
