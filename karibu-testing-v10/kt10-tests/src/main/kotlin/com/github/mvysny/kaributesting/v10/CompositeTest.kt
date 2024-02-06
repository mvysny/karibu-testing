package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout

@DynaTestDsl
internal fun DynaNodeGroup.compositeTests() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("Composite<Button> doesn't duplicate the button") {
        class MyButton : Composite<Button>()

        val btn = MyButton()
        btn._expectOne<Button>()
    }

    test("Composite<VerticalLayout> doesn't duplicate its children") {
        class MyLayout : Composite<VerticalLayout>() {
            init {
                content.add(Button("Foo"))
            }
        }

        val l = MyLayout()
        l._expectOne<Button>()
    }

    test("Composite<FlexLayout> doesn't duplicate its children") {
        class MyLayout : Composite<FlexLayout>() {
            init {
                content.add(Button("Foo"))
            }
        }

        val l = MyLayout()
        l._expectOne<Button>()
    }

    test("Composite<Div> doesn't duplicate its children") {
        class MyLayout : Composite<Div>() {
            init {
                content.add(Button("Foo"))
            }
        }

        val l = MyLayout()
        l._expectOne<Button>()
    }
}
