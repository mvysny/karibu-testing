// Distinct group so this and karibu-testing-v10:tests don't collapse to the same
// group:name module identity — see the note in karibu-testing-v10/tests/build.gradle.kts.
// These libs are never published, so the group is cosmetic.
group = "com.github.mvysny.kaributesting.v23"

dependencies {
    api(project(":karibu-testing-v10:tests")) {
        exclude(group = "com.vaadin")
    }
    api(libs.karibudsl23) {
        exclude(module = "javax.el")
    }

    api(project(":karibu-testing-v23"))
    api(libs.vaadin.stable.spring)
    compileOnly(libs.vaadin.stable.all)
}
