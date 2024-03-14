dependencies {
    api(project(":karibu-testing-v10:kt10-tests")) {
        exclude(group = "com.vaadin")
    }
    api(libs.karibu.dsl23)
    api(project(":karibu-testing-v23"))
    api(libs.vaadin.spring)
    compileOnly(libs.vaadin.v23next.all)
}
