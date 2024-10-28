package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

abstract class AbstractTabsTests {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class Children {
        @Test fun `None by default`() {
            Tabs()._expectNone<Tab>()
        }
        @Test fun `Expect tabs to be found`() {
            val tabs = Tabs()
            val tab1 = Tab("Tab1")
            val tab2 = Tab("Tab2")
            tabs.add(tab1, tab2)
            tabs._expect<Tab>(2)
        }
    }

    @Nested inner class lookup {
        @Test fun `lookup by label`() {
            val tabs = Tabs()
            tabs.add(Tab("foobar"))
            tabs._expect<Tab> { label = "foobar" }
        }
    }
}
