plugins {
    groovy
}

dependencies {
    api(project(":karibu-testing-v10-groovy"))

    // don't compile-depend on vaadin-core anymore: the app itself should manage Vaadin dependencies, for example
    // using the gradle-flow-plugin or direct dependency on vaadin-core. The reason is that the app may wish to use the
    // npm mode and exclude all webjars.
    // depend on vaadin instead of vaadin-core, to bring in Confirm Dialog and Grid Pro.
    compileOnly("com.vaadin:vaadin:${properties["vaadin14_version"]}") {
        // Webjars are only needed when running in Vaadin 13 compatibility mode
        listOf("com.vaadin.webjar", "org.webjars.bowergithub.insites",
                "org.webjars.bowergithub.polymer", "org.webjars.bowergithub.polymerelements",
                "org.webjars.bowergithub.vaadin", "org.webjars.bowergithub.webcomponents")
                .forEach { exclude(group = it) }
    }
    testImplementation("com.vaadin:vaadin:${properties["vaadin14_version"]}") {
        // Webjars are only needed when running in Vaadin 13 compatibility mode
        listOf("com.vaadin.webjar", "org.webjars.bowergithub.insites",
                "org.webjars.bowergithub.polymer", "org.webjars.bowergithub.polymerelements",
                "org.webjars.bowergithub.vaadin", "org.webjars.bowergithub.webcomponents")
                .forEach { exclude(group = it) }
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
    testImplementation("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("karibu-testing-v10-pro-groovy")
