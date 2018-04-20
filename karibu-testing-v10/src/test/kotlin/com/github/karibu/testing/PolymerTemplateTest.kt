package com.github.karibu.testing

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectList
import com.vaadin.flow.component.Component
import kotlin.test.expect
import kotlin.streams.*

class PolymerTemplateTest : DynaTest({
    beforeEach { MockVaadin.setup(Routes().autoDiscoverViews("com.github")) }

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
})
