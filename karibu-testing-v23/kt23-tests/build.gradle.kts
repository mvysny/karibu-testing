dependencies {
    api(project(":karibu-testing-v10:kt10-tests")) {
        exclude(group = "com.vaadin")
    }
    api(libs.karibudsl23) {
        exclude(module = "javax.el")
    }

    api(project(":karibu-testing-v23"))
    api(libs.vaadin.v24.spring)
    compileOnly(libs.vaadin.v24.all)
    api(libs.dynatest)
}
