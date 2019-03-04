package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.navigator.Navigator
import com.vaadin.server.ExternalResource
import com.vaadin.ui.Link
import com.vaadin.ui.UI
import java.lang.IllegalStateException

class LinkTest : DynaTest({
    beforeEach {
        MockVaadin.setup()
        UI.getCurrent().apply {
            navigator = Navigator(this, this)
            navigator.addView("myjavaview", LocatorJApiTest.MyJavaView::class.java)
        }
    }
    afterEach { MockVaadin.tearDown() }

    test("simple navigation") {
        Link("foo", ExternalResource("myjavaview"))._click()
        expectView<LocatorJApiTest.MyJavaView>()
    }

    test("navigation to external system fails") {
        expectThrows(IllegalArgumentException::class) {
            Link("foo", ExternalResource("https://www.github.com"))._click()
        }
    }

    test("clicking disabled link fails") {
        expectThrows(IllegalStateException::class, "The Link[DISABLED, caption='foo'] is not enabled") {
            Link("foo", ExternalResource("https://www.github.com")).apply {
                isEnabled = false
                _click()
            }
        }
    }
})
