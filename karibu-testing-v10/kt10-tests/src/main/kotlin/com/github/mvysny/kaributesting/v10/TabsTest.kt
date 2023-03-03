package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.kaributools.addColumnFor
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.treegrid.TreeGrid
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery
import com.vaadin.flow.data.provider.hierarchy.TreeData
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider
import com.vaadin.flow.data.renderer.NativeButtonRenderer
import java.util.stream.Stream
import kotlin.test.expect

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
}
