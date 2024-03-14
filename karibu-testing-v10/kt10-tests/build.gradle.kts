dependencies {
    api(project(":karibu-testing-v10"))

    // for testing purposes
    api(libs.dynatest)
    api(libs.slf4j.simple)
    api(libs.karibu.dsl)
    api(libs.vaadin.v14)

    api(libs.spring.boot.starter.test) {
        exclude(group = "ch.qos.logback")
    }
    api(libs.spring.boot.starter.web) {
        exclude(group = "ch.qos.logback")
    }
    api(libs.vaadin.spring)
    api(project(":karibu-testing-v10-spring"))

    // for testing out the NPM template loading from META-INF/resources/frontend/
    api(libs.addon.applayout)

    // to test that EnhancedDialog's components are discovered properly
    // Issue: https://github.com/mvysny/karibu-testing/issues/85
    implementation(libs.addon.enhanceddialog)
}
