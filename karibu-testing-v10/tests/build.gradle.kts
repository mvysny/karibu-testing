// Distinct group so this and karibu-testing-v23:tests don't collapse to the same
// group:name module identity: both are leaf-named `tests`, and v23:tests depends on
// this one, so identical coordinates make Gradle substitute one for the other and
// create a self-referential circular task dependency. These libs are never published,
// so the group is cosmetic. See DECISIONS.md.
group = "com.github.mvysny.kaributesting.v10"

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

    api(libs.vaadin.stable.all)

    api(libs.spring.boot.starter.test) {
        exclude(group = "ch.qos.logback")
    }
    api(libs.spring.boot.starter.web) {
        exclude(group = "ch.qos.logback")
    }
    api(libs.vaadin.stable.spring)
    api(project(":karibu-testing-v10-spring"))
}
