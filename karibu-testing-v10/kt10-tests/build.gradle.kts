dependencies {
    api(project(":karibu-testing-v10"))

    // for testing purposes
    api(libs.dynatest)
    api(libs.slf4j.simple)
    api(libs.karibudsl) {
        exclude(module = "javax.el")
    }
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("jakarta.el:jakarta.el-api:5.0.1")

    api(libs.vaadin.v24.all)

    api("org.springframework.boot:spring-boot-starter-test:3.0.4") {
        exclude(group = "ch.qos.logback")
    }
    api("org.springframework.boot:spring-boot-starter-web:3.0.4") {
        exclude(group = "ch.qos.logback")
    }
    api(libs.vaadin.v24.spring)
    api(project(":karibu-testing-v10-spring"))

    // don't test EnhancedDialog: it's deprecated and doesn't work with Vaadin 24: https://vaadin.com/directory/component/enhanced-dialog
    // Issue: https://github.com/mvysny/karibu-testing/issues/85
//    implementation("com.vaadin.componentfactory:enhanced-dialog:1.0.4")
}
