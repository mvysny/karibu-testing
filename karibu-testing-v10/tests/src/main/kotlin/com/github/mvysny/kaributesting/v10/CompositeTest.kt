package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class AbstractCompositeTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun `CompositeButton doesn't duplicate the button`() {
        class MyButton : Composite<Button>()

        val btn = MyButton()
        btn._expectOne<Button>()
    }

    @Test fun `CompositeVerticalLayout doesn't duplicate its children`() {
        class MyLayout : Composite<VerticalLayout>() {
            init {
                content.add(Button("Foo"))
            }
        }

        val l = MyLayout()
        l._expectOne<Button>()
    }

    @Test fun `CompositeFlexLayout doesn't duplicate its children`() {
        class MyLayout : Composite<FlexLayout>() {
            init {
                content.add(Button("Foo"))
            }
        }

        val l = MyLayout()
        l._expectOne<Button>()
    }

    @Test fun `CompositeDiv doesn't duplicate its children`() {
        class MyLayout : Composite<Div>() {
            init {
                content.add(Button("Foo"))
            }
        }

        val l = MyLayout()
        l._expectOne<Button>()
    }
}
