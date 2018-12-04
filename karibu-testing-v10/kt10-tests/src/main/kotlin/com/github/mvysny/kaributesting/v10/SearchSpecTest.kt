package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTest
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Label
import java.util.function.Predicate
import kotlin.test.expect

internal fun DynaNodeGroup.searchSpecTest() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("clazz") {
        val spec = SearchSpec(Button::class.java)
        expect(true) { spec.toPredicate()(Button())}
        expect(false) { spec.toPredicate()(Label())}
    }

    test("id") {
        val spec = SearchSpec(Component::class.java, id = "25")
        expect(true) { spec.toPredicate()(Button().apply { setId("25") })}
        expect(false) { spec.toPredicate()(Button().apply { setId("42") })}
        expect(false) { spec.toPredicate()(Button())}
    }

    test("caption") {
        val spec = SearchSpec(Component::class.java, caption = "foo")
        expect(true) { spec.toPredicate()(Button("foo"))}
        expect(false) { spec.toPredicate()(Button("bar"))}
        expect(false) { spec.toPredicate()(Button())}
    }

    test("text") {
        val spec = SearchSpec(Component::class.java, text = "foo")
        expect(true) { spec.toPredicate()(Button("foo"))}
        expect(false) { spec.toPredicate()(Button("bar"))}
        expect(false) { spec.toPredicate()(Button())}
        expect(true) { spec.toPredicate()(Text("foo"))}
        expect(false) { spec.toPredicate()(Text("bar"))}
        expect(false) { spec.toPredicate()(Text(""))}
    }

    test("predicates") {
        var spec = SearchSpec(Component::class.java).apply {
            predicates.add(Predicate { it -> it is Button })
        }
        expect(true) { spec.toPredicate()(Button()) }
        expect(false) { spec.toPredicate()(Label()) }
    }
}
