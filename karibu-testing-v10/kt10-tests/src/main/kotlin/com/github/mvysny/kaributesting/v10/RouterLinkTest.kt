package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.routerLink
import com.vaadin.flow.component.UI

internal fun DynaNodeGroup.routerLinkBatch() {
    test("simple") {
        MockVaadin.setup(Routes().apply { routes.add(TestingView::class.java) })
        UI.getCurrent().apply {
            routerLink(null, "testing", TestingView::class) {
                _click()
            }
        }
        // make sure that the navigation has been performed and there is the TestingView in the current UI
        _get<TestingView>()
    }
    test("disabled link cannot be clicked") {
        expectThrows(IllegalStateException::class, "The RouterLink[DISABLED, text='testing'] is not enabled") {
            UI.getCurrent().apply {
                routerLink(null, "testing", TestingView::class) {
                    isEnabled = false
                    _click()
                }
            }
        }
    }
}
