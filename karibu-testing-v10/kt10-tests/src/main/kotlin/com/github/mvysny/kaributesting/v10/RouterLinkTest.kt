package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.anchor
import com.github.mvysny.karibudsl.v10.routerLink
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.NotFoundException

internal fun DynaNodeGroup.routerLinkBatch() {
    beforeEach {
        MockVaadin.setup(Routes().apply { routes.add(TestingView::class.java) })
    }
    afterEach {
        MockVaadin.tearDown()
    }

    group("routerLink") {
        test("simple") {
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
        test("navigation to non-existing route blows immediately") {
            expectThrows(NotFoundException::class, """No route found for 'nonexisting': Couldn't find route for 'nonexisting'
Available routes: [testing TestingView]
If you'd like to revert back to the original Vaadin RouteNotFoundError, please remove this class from Routes.errorRoutes""") {
                UI.getCurrent().apply {
                    routerLink(null, "testing") {
                        element.setAttribute("href", "nonexisting")
                        _click()
                    }
                }
            }
        }
    }

    group("anchor") {
        test("simple") {
            UI.getCurrent().apply {
                anchor("testing") {
                    _click()
                }
            }
            // make sure that the navigation has been performed and there is the TestingView in the current UI
            _get<TestingView>()
        }
        test("disabled link cannot be clicked") {
            expectThrows(IllegalStateException::class, "The Anchor[DISABLED, text='testing', href='testing'] is not enabled") {
                UI.getCurrent().apply {
                    anchor("testing") {
                        isEnabled = false
                        _click()
                    }
                }
            }
        }
        test("navigation to non-existing route blows immediately") {
            expectThrows(NotFoundException::class, """No route found for 'nonexisting': Couldn't find route for 'nonexisting'
Available routes: [testing TestingView]
If you'd like to revert back to the original Vaadin RouteNotFoundError, please remove this class from Routes.errorRoutes""") {
                UI.getCurrent().apply {
                    anchor("nonexisting") {
                        _click()
                    }
                }
            }
        }
    }
}
