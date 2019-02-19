package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
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

    test("navigation to parametrized test") {
        navigateToView(ParametrizedView::class, 5)
        expect(ParametrizedView::class.java) { currentView }
        expectView<ParametrizedView>()
        expectThrows(AssertionError::class) {
            expectView<TestingView>()
        }
    }
}
