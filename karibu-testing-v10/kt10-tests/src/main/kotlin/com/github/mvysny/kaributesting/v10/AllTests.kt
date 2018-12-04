package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup

fun DynaNodeGroup.allTests() {
    group("basic utils") {
        basicUtilsTestbatch()
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
        locatorJTest()
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
}
