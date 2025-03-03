package com.github.mvysny.kaributesting.v10.spring

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.mock.MockedUI
import com.github.mvysny.kaributesting.v10.Routes
import com.github.mvysny.kaributesting.v10.TestingView
import com.github.mvysny.kaributools.navigateTo
import com.vaadin.flow.server.VaadinService
import com.vaadin.flow.server.VaadinServlet
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.spring.SpringServlet
import com.vaadin.flow.spring.SpringVaadinServletService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.web.context.WebApplicationContext
import kotlin.test.expect

@ExtendWith(SpringExtension::class)
@SpringBootTest
@WebAppConfiguration
@DirtiesContext
abstract class AbstractSpringTest {

    private val routes: Routes = Routes(mutableSetOf(TestingView::class.java))

    @Autowired
    private lateinit var ctx: ApplicationContext

    @BeforeEach
    fun setup() {
        val uiFactory: () -> MockedUI = { MockedUI() }
        val servlet: SpringServlet =
            MockSpringServlet(routes, ctx, uiFactory)
        MockVaadin.setup(uiFactory, servlet)
    }

    @Test
    fun testBasicEnv() {
        // check correct vaadin instances
        VaadinService.getCurrent() as SpringVaadinServletService

        // check that the context is set: https://github.com/mvysny/karibu-testing/issues/128
        expect(ctx) {
            VaadinServlet.getCurrent().servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE)
        }
        expect<Class<*>>(MockSpringServlet::class.java) { VaadinServlet.getCurrent().javaClass }
    }

    @Test
    fun testDestroyListenersCalled() {
        // check correct vaadin instances
        VaadinService.getCurrent() as SpringVaadinServletService

        // verify that the "destroy listeners" are called
        var called = 0
        VaadinSession.getCurrent().addSessionDestroyListener { called++ }
        MockVaadin.tearDown()
        expect(1) { called }
    }

    /**
     * Tests https://github.com/mvysny/karibu-testing/issues/184
     */
    @Test
    fun testRouteScopedComponent() {
        navigateTo<TestingView>() // to setup the route scope
        // if we don't navigate to TestingView, the scope isn't established and there's
        // no instance of RouteScopedComponent, and the bean retrieval would fail.
        val c = ctx.getBean(RouteScopedComponent::class.java)
        expect(true) { c != null }
    }

    @AfterEach
    fun tearDown() {
        MockVaadin.tearDown()
    }
}
