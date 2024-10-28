package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.anchor
import com.github.mvysny.karibudsl.v10.routerLink
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.NotFoundException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class  AbstractRouterLinkTests() {
    @BeforeEach fun fakeVaadin() {
        MockVaadin.setup(Routes().apply { routes.add(TestingView::class.java) })
    }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class routerLink {
        @Test fun simple() {
            UI.getCurrent().apply {
                routerLink(null, "testing", TestingView::class) {
                    _click()
                }
            }
            // make sure that the navigation has been performed and there is the TestingView in the current UI
            _get<TestingView>()
        }
        @Test fun `disabled link cannot be clicked`() {
            expectThrows(IllegalStateException::class, "The RouterLink[DISABLED, text='testing'] is not enabled") {
                UI.getCurrent().apply {
                    routerLink(null, "testing", TestingView::class) {
                        isEnabled = false
                        _click()
                    }
                }
            }
        }
        @Test fun `navigation to non-existing route blows immediately`() {
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

    @Nested inner class anchor {
        @Test fun simple() {
            UI.getCurrent().anchor("testing") {
                _click()
            }
            // make sure that the navigation has been performed and there is the TestingView in the current UI
            _get<TestingView>()
        }
        @Test fun toPrettyString() {
            expect("Anchor[text='testing', href='testing']") {
                UI.getCurrent().anchor("testing").toPrettyString()
            }
            expect("Anchor[DISABLED, text='testing', href='testing']") {
                UI.getCurrent().anchor("testing") { isEnabled = false }.toPrettyString()
            }
        }
        @Test fun `disabled link cannot be clicked`() {
            expectThrows(IllegalStateException::class, "The Anchor[DISABLED, text='testing', href='testing'] is not enabled") {
                UI.getCurrent().anchor("testing") {
                    isEnabled = false
                    _click()
                }
            }
        }
        @Test fun `navigation to non-existing route blows immediately`() {
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
