package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.vaadin.flow.server.Version

fun DynaNodeGroup.allTests() {
    group("basic utils") {
        basicUtilsTestbatch()
    }
    group("combo box") {
        comboBoxTestbatch()
    }
    group("grid") {
        gridTestbatch()
    }
    group("locator addons") {
        locatorAddonsTestbatch()
    }
    group("locatorj") {
        locatorJTest()
    }
    group("locator") {
        locatorTest()
    }
    group("mock vaadin") {
        mockVaadinTest()
    }
    group("polymer template") {
        polymerTemplateTest()
    }
    group("pretty print tree") {
        prettyPrintTreeTest()
    }
    group("search spec") {
        searchSpecTest()
    }
    group("notifications") {
        notificationsTestBattery()
    }
    group("context menu") {
        contextMenuTestbatch()
    }
}
