package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectList
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import kotlin.streams.toList
import kotlin.test.expect

@DynaTestDsl
internal fun DynaNodeGroup.polymerTemplateTest() {
    lateinit var routes: Routes
    beforeGroup { routes = Routes().autoDiscoverViews("com.github") }
    beforeEach { MockVaadin.setup(routes) }
    afterEach { MockVaadin.tearDown() }

    test("simple instantiation fills in @Id-annotated components") {
        val list = ReviewsList()
        list.addReview._click()
        // the text will be empty since the components instantiated by the PolymerTemplate will be incomplete:
        // https://github.com/mvysny/karibu-testing/issues/1
        expect("") { list.addReview.text }
        expect("") { list.header.text }
        expect("") { list.search.label ?: "" }
    }

    // because of https://github.com/mvysny/karibu-testing/issues/1,
    // there must be no workaround for the polymer template to list any children
    test("lookup on template should find no components") {
        val list = ReviewsList()
        expectList() { list.children.toList() }
        expectList(list) { list._find<Component>() }
    }

    // https://github.com/mvysny/karibu-testing/issues/35
    test("when lookup of a component fails, notify user that it might be because of PolymerTemplate") {
        UI.getCurrent().add(ReviewsList())
        expectThrows(AssertionError::class, "Karibu-Testing is not able to look up components from inside of PolymerTemplate. Please see https://github.com/mvysny/karibu-testing/tree/master/karibu-testing-v10#polymer-templates") {
            _get<Button>()
        }
    }

    // https://github.com/mvysny/karibu-testing/issues/35
    test("when a lookup of a component fails and there is no PolymerTemplate, don't mention anything") {
        val ex: AssertionError = expectThrows(AssertionError::class) {
            _get<Button>()
        }
        expect(false, ex.message) { ex.message!!.contains("PolymerTemplate", true) }
    }
}
