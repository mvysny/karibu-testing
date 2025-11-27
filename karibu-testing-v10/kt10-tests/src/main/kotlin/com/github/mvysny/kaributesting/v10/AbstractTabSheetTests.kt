package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.tabs.TabSheet
import com.vaadin.flow.component.tabs.Tabs
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractTabSheetTests {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun retrieveInnerTabs() {
        val t = TabSheet()
        expect(t._get<Tabs>()) { t._tabs }
    }

    @Test fun unselectedContentsNotFound() {
        val t = TabSheet()
        t.add("tab1", Button("Tab1"))
        t.add("tab2", Button("Tab2"))
        t._expectOne<Button> { text = "Tab1" }
        t._expectNone<Button> { text = "Tab2" }

        t.selectedIndex = 1
        t._expectOne<Button> { text = "Tab2" }
        t._expectNone<Button> { text = "Tab1" }
    }

    // tests https://github.com/mvysny/karibu-testing/issues/198
    @Test fun prefixAndSuffixComponentsFound() {
        val t = TabSheet()
        t.prefixComponent = Button("Prefix")
        t.suffixComponent = Button("Suffix")
        t._expectOne<Button> { text = "Prefix" }
        t._expectOne<Button> { text = "Suffix" }
    }

    // tests https://github.com/mvysny/karibu-testing/issues/198
    @Test fun prefixAndSuffixComponentsFound2() {
        val t = TabSheet()
        t.prefixComponent = Button("Prefix")
        t.suffixComponent = Button("Suffix")
        t.add("tab1", Span("Hello"))
        t._expectOne<Button> { text = "Prefix" }
        t._expectOne<Button> { text = "Suffix" }
    }
}
