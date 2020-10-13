package com.github.mvysny.kaributesting.v10.spring;

import com.github.mvysny.kaributesting.v10.mock.MockVaadinHelper;
import com.github.mvysny.kaributesting.v10.Routes;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.spring.SpringServlet;
import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;

/**
 * Makes sure that the {@link #routes} are properly registered,
 * and that {@link MockSpringServletService} is used instead of vanilla {@link com.vaadin.flow.spring.SpringVaadinServletService}.
 * @author mavi
 */
public class MockSpringServlet extends SpringServlet {
    @NotNull
    public final Routes routes;
    @NotNull
    public final ApplicationContext ctx;
    @NotNull
    public final Function0<UI> uiFactory;

    public MockSpringServlet(@NotNull Routes routes, @NotNull ApplicationContext ctx, @NotNull Function0<UI> uiFactory) {
        super(ctx, false);
        this.ctx = ctx;
        this.routes = routes;
        this.uiFactory = uiFactory;
    }

    @Override
    protected DeploymentConfiguration createDeploymentConfiguration() throws ServletException {
        MockVaadinHelper.mockFlowBuildInfo(this);
        return super.createDeploymentConfiguration();
    }

    @Override
    protected VaadinServletService createServletService(DeploymentConfiguration deploymentConfiguration) throws ServiceException {
        final VaadinServletService service = new MockSpringServletService(this, deploymentConfiguration, ctx, uiFactory);
        service.init();
        routes.register((VaadinServletContext) service.getContext());
        return service;
    }
}
