package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.Routes
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.Route
import groovy.transform.CompileStatic

/**
 * For {@link RouterLinkUtilsTest}.
 */
@Route("")
@CompileStatic
class DummyRootRoute extends Div {
    public static final Routes routes = new Routes().autoDiscoverViews("com.github.mvysny.kaributesting.v10.groovy")
}
