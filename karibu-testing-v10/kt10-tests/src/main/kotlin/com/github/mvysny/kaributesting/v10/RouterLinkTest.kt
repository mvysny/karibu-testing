package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeDsl
import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.anchor
import com.github.mvysny.karibudsl.v10.routerLink
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.NotFoundException
import kotlin.test.expect

@DynaNodeDsl
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
Available routes: [TestingView at '/testing']
If you'd like to revert back to the original Vaadin RouteNotFoundError, please remove the class com.github.mvysny.kaributesting.v10.MockRouteNotFoundError from Routes.errorRoutes""") {
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
            UI.getCurrent().anchor("testing") {
                _click()
            }
            // make sure that the navigation has been performed and there is the TestingView in the current UI
            _get<TestingView>()
        }
        test("toPrettyString") {
            expect("Anchor[text='testing', href='testing']") {
                UI.getCurrent().anchor("testing").toPrettyString()
            }
            expect("Anchor[DISABLED, text='testing', href='testing']") {
                UI.getCurrent().anchor("testing") { isEnabled = false }.toPrettyString()
            }
        }
        test("disabled link cannot be clicked") {
            expectThrows(IllegalStateException::class, "The Anchor[DISABLED, text='testing', href='testing'] is not enabled") {
                UI.getCurrent().anchor("testing") {
                    isEnabled = false
                    _click()
                }
            }
        }
        test("navigation to non-existing route blows immediately") {
            expectThrows(NotFoundException::class, """No route found for 'nonexisting': Couldn't find route for 'nonexisting'
Available routes: [TestingView at '/testing']
If you'd like to revert back to the original Vaadin RouteNotFoundError, please remove the class com.github.mvysny.kaributesting.v10.MockRouteNotFoundError from Routes.errorRoutes""") {
                UI.getCurrent().apply {
                    anchor("nonexisting") {
                        _click()
                    }
                }
            }
        }
    }
}
