package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.kaributesting.v23._select
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.TabSheet
import com.vaadin.flow.component.tabs.Tabs
import kotlin.test.expect


@DynaTestDsl
internal fun DynaNodeGroup.tabsTests() {
    group("_select()") {
        group("Tabs") {
            selectTests(
                { Tabs() },
                { tabs, tab -> tabs.add(tab) },
                { it.selectedTab })
        }
        group("TabSheet") {
            selectTests(
                { TabSheet() },
                { tabs, tab -> tabs.add(tab, Span("contents of $tab")) },
                { it.selectedTab })
        }
    }
}

@DynaTestDsl
private fun <T : Component> DynaNodeGroup.selectTests(
    create: () -> T,
    add: (T, Tab) -> Unit,
    selected: (T) -> Tab
) {
    test("select already selected tab succeeds") {
        val t = create()
        val tab = Tab("Foo")
        add(t, tab)
        expect(tab) { selected(t) }

        tab._select()
        expect(tab) { selected(t) }
    }

    test("select enabled+visible tab succeeds") {
        val t = create()
        val tab = Tab("Foo")
        add(t, tab)
        val tab2 = Tab("Bar")
        add(t, tab2)
        expect(tab) { selected(t) }

        tab2._select()
        expect(tab2) { selected(t) }
    }

    test("select disabled tab fails") {
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

    test("select invisible tab fails") {
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
