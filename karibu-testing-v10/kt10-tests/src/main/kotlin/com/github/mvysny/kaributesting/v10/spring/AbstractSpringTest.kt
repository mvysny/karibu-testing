package com.github.mvysny.kaributesting.v10.spring

import com.github.mvysny.dynatest.jvmVersion
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.ReviewsList
import com.github.mvysny.kaributesting.v10.mock.MockedUI
import com.github.mvysny.kaributesting.v10.Routes
import com.vaadin.flow.server.VaadinService
import com.vaadin.flow.server.VaadinServlet
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.spring.SpringServlet
import com.vaadin.flow.spring.SpringVaadinServletService
import com.vaadin.flow.spring.SpringVaadinSession
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assumptions.assumeTrue
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
abstract class AbstractSpringTest(val vaadinVersion: Int) {

    private val routes: Routes = Routes()

    @Autowired
    private lateinit var ctx: ApplicationContext

    @BeforeEach
    fun setup() {
        assumeTrue(vaadinVersion < 23 || jvmVersion >= 11, "Vaadin 23+ only supports JDK 11+")

        val uiFactory: () -> MockedUI = { MockedUI() }
        val servlet: SpringServlet =
            MockSpringServlet(routes, ctx, uiFactory)
        MockVaadin.setup(uiFactory, servlet)
    }

    @Test
    fun testBasicEnv() {
        // check correct vaadin instances
        VaadinSession.getCurrent() as SpringVaadinSession
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
        VaadinSession.getCurrent() as SpringVaadinSession
        VaadinService.getCurrent() as SpringVaadinServletService

        // verify that the destroy listeners are called
        var called = 0
        (VaadinSession.getCurrent() as SpringVaadinSession).addDestroyListener { called++ }
        MockVaadin.tearDown()
        expect(1) { called }
    }

    /**
     * Tests for https://github.com/mvysny/karibu-testing/issues/65
     */
    @Test
    fun testPolymerTemplateComponent() {
        ReviewsList()
    }

    @AfterEach
    fun tearDown() {
        MockVaadin.tearDown()
    }
}
