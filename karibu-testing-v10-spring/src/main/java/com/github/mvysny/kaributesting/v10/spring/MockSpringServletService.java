package com.github.mvysny.kaributesting.v10.spring;

import com.github.mvysny.kaributesting.v10.mock.MockInstantiator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.SpringVaadinServletService;
import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;

/**
 * A mocking service that performs three very important tasks:
 * <ul>
 *     <li>Overrides {@link #isAtmosphereAvailable} to tell Vaadin that we don't have Atmosphere (otherwise Vaadin will crash)</li>
 *     <li>Provides some dummy value as a root ID via {@link #getMainDivId} (otherwise the mocked servlet env will crash).</li>
 *     <li>Provides a {@link MockSpringVaadinSession} instead of {@link com.vaadin.flow.spring.SpringVaadinSession}.</li>
 * </ul>
 * The class is intentionally opened, to be extensible in user's library.
 */
public class MockSpringServletService extends SpringVaadinServletService {
    /**
     * Creates new UIs.
     */
    @NotNull
    private final Function0<UI> uiFactory;

    /**
     * Creates new testing service.
     * @param servlet the testing servlet
     * @param deploymentConfiguration the configuration to use
     * @param ctx the Spring application context
     * @param uiFactory     Creates new UIs.
     */
    public MockSpringServletService(@NotNull MockSpringServlet servlet,
                                    @NotNull DeploymentConfiguration deploymentConfiguration,
                                    @NotNull ApplicationContext ctx,
                                    @NotNull Function0<UI> uiFactory) {
        super(servlet, deploymentConfiguration, ctx);
        this.uiFactory = uiFactory;
    }

    @Override
    protected boolean isAtmosphereAvailable() {
        return false;
    }

    @Override
    public String getMainDivId(VaadinSession session, VaadinRequest request) {
        return "ROOT-1";
    }

    @Override
    protected VaadinSession createVaadinSession(VaadinRequest request) {
        return new MockSpringVaadinSession(this, uiFactory);
    }

    @Override
    public Instantiator getInstantiator() {
        return MockInstantiator.create(super.getInstantiator());
    }
}
