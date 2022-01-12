package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.contextmenu.ContextMenu
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import groovy.transform.CompileStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Merely a test of the API. No need to test the implementation, it is thoroughly tested elsewhere.
 * @author mavi
 */
@CompileStatic
class ContextMenuUtilsTest {
    @BeforeEach void setup() {
        MockVaadin.setup()
    }
    @AfterEach void teardown() { MockVaadin.tearDown() }

    @Test
    void apiTest() {
        def menu = new ContextMenu()
        menu.addItem("foo")
        menu.setTarget(new Button())
        menu._clickItemWithCaption("foo")
        def gridMenu = new GridContextMenu<String>()
        gridMenu.setTarget(new Grid())
        gridMenu.addItem("foo")
        gridMenu._clickItemWithCaption("foo", "bar")
        menu.addItem("foo")._click()
        gridMenu.addItem("foo")._click("bar")
    }
}
