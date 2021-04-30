dependencies {
    api(project(":karibu-testing-v10"))

    // for testing purposes
    api("com.github.mvysny.dynatest:dynatest-engine:${properties["dynatest_version"]}")
    api("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
    api("com.github.mvysny.karibudsl:karibu-dsl:${properties["karibudsl_version"]}")
    api("com.vaadin:vaadin:${properties["vaadin14_version"]}") {
        // Webjars are only needed when running in Vaadin 13 compatibility mode
        listOf("com.vaadin.webjar", "org.webjars.bowergithub.insites",
                "org.webjars.bowergithub.polymer", "org.webjars.bowergithub.polymerelements",
                "org.webjars.bowergithub.vaadin", "org.webjars.bowergithub.webcomponents")
                .forEach { exclude(group = it) }
    }

    api("org.springframework.boot:spring-boot-starter-test:2.3.0.RELEASE") {
        exclude(group = "ch.qos.logback")
    }
    api("org.springframework.boot:spring-boot-starter-web:2.3.0.RELEASE") {
        exclude(group = "ch.qos.logback")
    }
    api("com.vaadin:vaadin-spring:12.2.0")
    api(project(":karibu-testing-v10-spring"))

    // for testing out the NPM template loading from META-INF/resources/frontend/
    api("com.github.appreciated:app-layout-addon:4.0.0.beta5")
}
