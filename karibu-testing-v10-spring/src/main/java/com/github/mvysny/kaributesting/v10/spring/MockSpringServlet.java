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
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * Makes sure that the {@link #routes} are properly registered,
 * and that {@link MockSpringServletService} is used instead of vanilla {@link com.vaadin.flow.spring.SpringVaadinServletService}.
 * @author mavi
 */
public class MockSpringServlet extends SpringServlet {
    /**
     * The routes registered to the application.
     */
    @NotNull
    public final Routes routes;
    /**
     * The application context.
     */
    @NotNull
    public final ApplicationContext ctx;
    /**
     * Creates new UIs.
     */
    @NotNull
    public final Function0<UI> uiFactory;

    /**
     * Creates new servlet.
     * @param routes The routes registered to the application.
     * @param ctx The application context.
     * @param uiFactory Creates new UIs.
     */
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

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        // some apps retrieve Spring's WebApplicationContext from the ServletContext,
        // via WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE.
        servletConfig.getServletContext().setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ctx);
    }
}
