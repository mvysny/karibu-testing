package com.github.karibu.testing.v10.javasupport;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.startup.RouteRegistry;

/**
 * This SAM hides kotlin interfaces from Java/Groovy users
 */
@FunctionalInterface
public interface ServiceFactory {
    VaadinServletService provide(VaadinServlet vaadinServlet,
                                 DeploymentConfiguration deploymentConfiguration,
                                 RouteRegistry routeRegistry);
}