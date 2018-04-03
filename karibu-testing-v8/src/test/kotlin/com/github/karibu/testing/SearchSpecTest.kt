package com.github.karibu.testing

import com.github.mvysny.dynatest.DynaTest
import com.vaadin.ui.Button
import com.vaadin.ui.Component
import com.vaadin.ui.Label
import java.util.function.Predicate
import kotlin.test.expect

class SearchSpecTest : DynaTest({
    beforeEach { MockVaadin.setup() }

    test("clazz") {
        val spec = SearchSpec(Button::class.java)
        expect(true) { spec.toPredicate()(Button())}
        expect(false) { spec.toPredicate()(Label())}
    }

    test("id") {
        val spec = SearchSpec(Component::class.java, id = "25")
        expect(true) { spec.toPredicate()(Button().apply { id = "25" })}
        expect(false) { spec.toPredicate()(Button().apply { id = "42" })}
        expect(false) { spec.toPredicate()(Button())}
    }

    test("caption") {
        val spec = SearchSpec(Component::class.java, caption = "foo")
        expect(true) { spec.toPredicate()(Button("foo"))}
        expect(false) { spec.toPredicate()(Button("bar"))}
        expect(false) { spec.toPredicate()(Button())}
    }

    test("styles") {
        val spec = SearchSpec(Component::class.java, styles = "large primary")
        expect(true) { spec.toPredicate()(Button().apply { styleName = "large primary red" })}
        expect(true) { spec.toPredicate()(Button().apply { styleName = "primary large" })}
        expect(true) { spec.toPredicate()(Button().apply { styleName = "large primary" })}
        expect(false) { spec.toPredicate()(Button().apply { styleName = "primary" })}
        expect(false) { spec.toPredicate()(Button().apply { styleName = "large" })}
        expect(false) { spec.toPredicate()(Button().apply { styleName = "red" })}
        expect(false) { spec.toPredicate()(Button())}
    }

    test("predicates") {
        var spec = SearchSpec(Component::class.java).apply {
            predicates.add(Predicate { it -> it is Button })
        }
        expect(true) { spec.toPredicate()(Button()) }
        expect(false) { spec.toPredicate()(Label()) }
    }
})