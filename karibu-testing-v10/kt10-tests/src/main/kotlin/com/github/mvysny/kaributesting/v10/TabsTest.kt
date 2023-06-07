package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs

@DynaTestDsl
internal fun DynaNodeGroup.tabsTestbatch() {

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("Children") {
        test("None by default") {
            Tabs()._expectNone<Tab>()
        }
        test("Expect tabs to be found") {
            val tabs = Tabs()
            val tab1 = Tab("Tab1")
            val tab2 = Tab("Tab2")
            tabs.add(tab1, tab2)
            tabs._expect<Tab>(2)
        }
    }

    group("lookup") {
        test("lookup by label") {
            val tabs = Tabs()
            tabs.add(Tab("foobar"))
            tabs._expect<Tab> { label = "foobar" }
        }
    }
}
