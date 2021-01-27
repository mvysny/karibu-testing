package com.github.mvysny.kaributesting.v10.mock

import com.vaadin.flow.component.UI
import com.vaadin.flow.di.Instantiator
import com.vaadin.flow.function.DeploymentConfiguration
import com.vaadin.flow.server.VaadinRequest
import com.vaadin.flow.server.VaadinServlet
import com.vaadin.flow.server.VaadinServletService
import com.vaadin.flow.server.VaadinSession

/**
 * A mocking service that performs three very important tasks:
 * * Overrides [isAtmosphereAvailable] to tell Vaadin that we don't have Atmosphere (otherwise Vaadin will crash)
 * * Provides some dummy value as a root ID via [getMainDivId] (otherwise the mocked servlet env will crash).
 * * Provides a [MockVaadinSession].
 * The class is intentionally opened, to be extensible in user's library.
 *
 * To register your custom `MockService` instance, override [MockVaadinServlet.createServletService].
 */
public open class MockService(servlet: VaadinServlet,
                              deploymentConfiguration: DeploymentConfiguration,
                              public val uiFactory: () -> UI = { MockedUI() }
) : VaadinServletService(servlet, deploymentConfiguration) {
    // need to have this override. Setting `VaadinService.atmosphereAvailable` to false via
    // reflection after the servlet has been initialized is too late, since Atmo is initialized
    // in VaadinService.init().
    override fun isAtmosphereAvailable(): Boolean = false
    override fun getMainDivId(session: VaadinSession?, request: VaadinRequest?): String = "ROOT-1"
    override fun createVaadinSession(request: VaadinRequest): VaadinSession = MockVaadinSession(this, uiFactory)
    override fun getInstantiator(): Instantiator = MockInstantiator.create(super.getInstantiator())
}
