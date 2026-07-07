package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributesting.v23._select
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.TabSheet
import com.vaadin.flow.component.tabs.Tabs
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractTabsTests() {
    @Nested inner class SelectTests {
        @Nested inner class TabsTests : AbstractTabsSelectTests<Tabs>(
            { Tabs() },
            { tabs, tab -> tabs.add(tab) },
            { it.selectedTab }
        )
        @Nested inner class TabSheetTests : AbstractTabsSelectTests<TabSheet>(
            { TabSheet() },
            { tabs, tab -> tabs.add(tab, Span("contents of $tab")) },
            { it.selectedTab }
        )
    }
}

abstract class AbstractTabsSelectTests<T : Component>(
    val create: () -> T,
    val add: (T, Tab) -> Unit,
    val selected: (T) -> Tab
) {
    @Test fun `select already selected tab succeeds`() {
        val t = create()
        val tab = Tab("Foo")
        add(t, tab)
        expect(tab) { selected(t) }

        tab._select()
        expect(tab) { selected(t) }
    }

    @Test fun `select enabled+visible tab succeeds`() {
        val t = create()
        val tab = Tab("Foo")
        add(t, tab)
        val tab2 = Tab("Bar")
        add(t, tab2)
        expect(tab) { selected(t) }

        tab2._select()
        expect(tab2) { selected(t) }
    }

    @Test fun `select disabled tab fails`() {
        val t = create()
        val tab = Tab("Foo")
        add(t, tab)
        val tab2 = Tab("Bar")
        tab2.isEnabled = false
        add(t, tab2)
        expect(tab) { selected(t) }

        expectThrows<IllegalStateException>("DISABLED, label='Bar', Tab{Bar}] is not enabled") {
            tab2._select()
        }
        expect(tab) { selected(t) }
    }

    @Test fun `select invisible tab fails`() {
        val t = create()
        val tab = Tab("Foo")
        add(t, tab)
        val tab2 = Tab("Bar")
        tab2.isVisible = false
        add(t, tab2)
        expect(tab) { selected(t) }

        expectThrows<IllegalStateException>("INVIS, label='Bar', Tab{Bar}] is not effectively visible") {
            tab2._select()
        }
        expect(tab) { selected(t) }
    }
}
