dependencies {
    api(project(":karibu-testing-v10"))

    // for testing purposes
    api("com.github.mvysny.dynatest:dynatest:${properties["dynatest_version"]}")
    api("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
    api("com.github.mvysny.karibudsl:karibu-dsl:${properties["karibudsl11_version"]}") {
        exclude(module = "javax.el")
    }
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("jakarta.el:jakarta.el-api:5.0.1")

    api("com.vaadin:vaadin:${properties["vaadin14_version"]}")

    api("org.springframework.boot:spring-boot-starter-test:3.0.0") {
        exclude(group = "ch.qos.logback")
    }
    api("org.springframework.boot:spring-boot-starter-web:3.0.0") {
        exclude(group = "ch.qos.logback")
    }
    api("com.vaadin:vaadin-spring:${properties["vaadin_spring_version"]}")
    api(project(":karibu-testing-v10-spring"))

    // to test that EnhancedDialog's components are discovered properly
    // Issue: https://github.com/mvysny/karibu-testing/issues/85
    implementation("com.vaadin.componentfactory:enhanced-dialog:1.0.4")
}
