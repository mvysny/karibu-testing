dependencies {
    api(project(":karibu-testing-v10:kt10-tests")) {
        exclude(group = "com.vaadin")
    }
    api(libs.karibudsl23) {
        exclude(module = "javax.el")
    }

    api(project(":karibu-testing-v23"))
    api(libs.vaadin.stable.spring)
    compileOnly(libs.vaadin.stable.all)
}
