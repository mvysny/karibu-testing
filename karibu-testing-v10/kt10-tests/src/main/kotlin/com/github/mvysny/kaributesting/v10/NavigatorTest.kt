package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.karibudsl.v10.navigateToView
import kotlin.test.expect

internal fun DynaNodeGroup.navigatorTest() {

    beforeEach { MockVaadin.setup(Routes().autoDiscoverViews("com.github")) }
    afterEach { MockVaadin.tearDown() }

    test("navigation") {
        navigateToView<TestingView>()
        expect(TestingView::class.java) { currentView }
        expectView<TestingView>()
    }
}
