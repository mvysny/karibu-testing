dependencies {
    api(project(":karibu-testing-v10"))

    // for testing purposes
    api(libs.dynatest)
    api(libs.slf4j.simple)
    api("com.github.mvysny.karibudsl:karibu-dsl:${properties["karibudsl11_version"]}") {
        exclude(module = "javax.el")
    }
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("jakarta.el:jakarta.el-api:5.0.1")

    api("com.vaadin:vaadin:${properties["vaadin24_version"]}")

    api("org.springframework.boot:spring-boot-starter-test:3.0.4") {
        exclude(group = "ch.qos.logback")
    }
    api("org.springframework.boot:spring-boot-starter-web:3.0.4") {
        exclude(group = "ch.qos.logback")
    }
    api("com.vaadin:vaadin-spring:${properties["vaadin24_version_spring"]}")
    api(project(":karibu-testing-v10-spring"))

    // don't test EnhancedDialog: it's deprecated and doesn't work with Vaadin 24: https://vaadin.com/directory/component/enhanced-dialog
    // Issue: https://github.com/mvysny/karibu-testing/issues/85
//    implementation("com.vaadin.componentfactory:enhanced-dialog:1.0.4")
}
