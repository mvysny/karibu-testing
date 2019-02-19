package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.karibudsl.v8.navigateToView
import com.vaadin.navigator.Navigator
import com.vaadin.ui.UI

class NavigatorTest : DynaTest({
    beforeEach {
        MockVaadin.setup()
        UI.getCurrent().apply {
            navigator = Navigator(this, this)
            navigator.addView("myjavaview", LocatorJApiTest.MyJavaView::class.java)
        }
    }
    afterEach { MockVaadin.tearDown() }

    test("expect view") {
        UI.getCurrent().navigator.navigateTo("myjavaview")
        expectView<LocatorJApiTest.MyJavaView>()
    }
})
