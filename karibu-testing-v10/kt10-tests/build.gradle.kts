dependencies {
    api(project(":karibu-testing-v10"))

    // for testing purposes
    api(libs.junit.jupiterapi)
    api(libs.slf4j.simple)
    api(libs.karibudsl) {
        exclude(module = "javax.el")
    }
    implementation(libs.hibernate.validator)
    implementation(libs.jakarta.el.api)

    api(libs.vaadin.v24.all)

    api(libs.spring.boot.starter.test) {
        exclude(group = "ch.qos.logback")
    }
    api(libs.spring.boot.starter.web) {
        exclude(group = "ch.qos.logback")
    }
    api(libs.vaadin.v24.spring)
    api(project(":karibu-testing-v10-spring"))

    // don't test EnhancedDialog: it's deprecated and doesn't work with Vaadin 24: https://vaadin.com/directory/component/enhanced-dialog
    // Issue: https://github.com/mvysny/karibu-testing/issues/85
//    implementation("com.vaadin.componentfactory:enhanced-dialog:1.0.4")
}
