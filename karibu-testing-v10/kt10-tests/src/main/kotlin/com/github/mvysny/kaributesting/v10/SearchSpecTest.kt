package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.kaributools.LabelWrapper
import com.github.mvysny.kaributools.label
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.html.Input
import com.vaadin.flow.component.html.NativeLabel
import com.vaadin.flow.component.listbox.ListBox
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.timepicker.TimePicker
import java.util.function.Predicate
import kotlin.test.expect

@DynaTestDsl
internal fun DynaNodeGroup.searchSpecTest() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("clazz") {
        val spec = SearchSpec(Button::class.java)
        expect(true) { spec.toPredicate()(Button())}
        expect(false) { spec.toPredicate()(NativeLabel())}
    }

    test("id") {
        val spec = SearchSpec(Component::class.java, id = "25")
        expect(true) { spec.toPredicate()(Button().apply { setId("25") }) }
        expect(false) { spec.toPredicate()(Button().apply { setId("42") }) }
        expect(false) { spec.toPredicate()(Button()) }
    }

    test("caption") {
        val spec = SearchSpec(Component::class.java, caption = "foo")
        expect(true) { spec.toPredicate()(Button("foo")) }
        expect(false) { spec.toPredicate()(Button("bar")) }
        expect(false) { spec.toPredicate()(Button()) }
        expect(true) { spec.toPredicate()(Checkbox("foo")) }
        expect(false) { spec.toPredicate()(Checkbox("bar")) }
        expect(false) { spec.toPredicate()(Checkbox()) }
        expect(true) { spec.toPredicate()(CheckboxGroup<Int>().apply { label = "foo" }) }
        expect(false) { spec.toPredicate()(CheckboxGroup<Int>().apply { label = "bar" }) }
        expect(false) { spec.toPredicate()(CheckboxGroup<Int>()) }
        expect(true) { spec.toPredicate()(Select<Int>().apply { label = "foo" }) }
        expect(false) { spec.toPredicate()(Select<Int>().apply { label = "bar" }) }
        expect(false) { spec.toPredicate()(Select<Int>()) }
        expect(true) { spec.toPredicate()(ListBox<Int>().apply { label = "foo" }) }
        expect(false) { spec.toPredicate()(ListBox<Int>().apply { label = "bar" }) }
        expect(false) { spec.toPredicate()(ListBox<Int>()) }
        expect(true) { spec.toPredicate()(RadioButtonGroup<Int>().apply { label = "foo" }) }
        expect(false) { spec.toPredicate()(RadioButtonGroup<Int>().apply { label = "bar" }) }
        expect(false) { spec.toPredicate()(RadioButtonGroup<Int>()) }
        // tests CustomField
        expect(true) { spec.toPredicate()(LabelWrapper("foo")) }
        expect(false) { spec.toPredicate()(LabelWrapper("bar")) }
        expect(false) { spec.toPredicate()(LabelWrapper("")) }

        expect(true) { spec.toPredicate()(Input().apply { label = "foo" }) }
        expect(false) { spec.toPredicate()(Input().apply { label = "bar" }) }
        expect(false) { spec.toPredicate()(Input()) }
        expect(true) { spec.toPredicate()(TextField("foo")) }
        expect(false) { spec.toPredicate()(TextField("bar")) }
        expect(false) { spec.toPredicate()(TextField()) }
        expect(true) { spec.toPredicate()(TextArea("foo")) }
        expect(false) { spec.toPredicate()(TextArea("bar")) }
        expect(false) { spec.toPredicate()(TextArea()) }
        expect(true) { spec.toPredicate()(TimePicker("foo")) }
        expect(false) { spec.toPredicate()(TimePicker("bar")) }
        expect(false) { spec.toPredicate()(TimePicker()) }
        expect(true) { spec.toPredicate()(DatePicker("foo")) }
        expect(false) { spec.toPredicate()(DatePicker("bar")) }
        expect(false) { spec.toPredicate()(DatePicker()) }
        expect(true) { spec.toPredicate()(ComboBox<Int>("foo")) }
        expect(false) { spec.toPredicate()(ComboBox<Int>("bar")) }
        expect(false) { spec.toPredicate()(ComboBox<Int>()) }
    }

    test("label") {
        val spec = SearchSpec(Component::class.java, label = "foo")
        expect(false) { spec.toPredicate()(Button("foo")) }
        expect(false) { spec.toPredicate()(Button("bar")) }
        expect(false) { spec.toPredicate()(Button()) }
        expect(true) { spec.toPredicate()(Checkbox("foo")) }
        expect(false) { spec.toPredicate()(Checkbox("bar")) }
        expect(false) { spec.toPredicate()(Checkbox()) }
        expect(true) { spec.toPredicate()(CheckboxGroup<Int>().apply { label = "foo" }) }
        expect(false) { spec.toPredicate()(CheckboxGroup<Int>().apply { label = "bar" }) }
        expect(false) { spec.toPredicate()(CheckboxGroup<Int>()) }
        expect(true) { spec.toPredicate()(Select<Int>().apply { label = "foo" }) }
        expect(false) { spec.toPredicate()(Select<Int>().apply { label = "bar" }) }
        expect(false) { spec.toPredicate()(Select<Int>()) }
        expect(true) { spec.toPredicate()(ListBox<Int>().apply { label = "foo" }) }
        expect(false) { spec.toPredicate()(ListBox<Int>().apply { label = "bar" }) }
        expect(false) { spec.toPredicate()(ListBox<Int>()) }
        expect(true) { spec.toPredicate()(RadioButtonGroup<Int>().apply { label = "foo" }) }
        expect(false) { spec.toPredicate()(RadioButtonGroup<Int>().apply { label = "bar" }) }
        expect(false) { spec.toPredicate()(RadioButtonGroup<Int>()) }
        // tests CustomField
        expect(true) { spec.toPredicate()(LabelWrapper("foo")) }
        expect(false) { spec.toPredicate()(LabelWrapper(label = "bar")) }
        expect(false) { spec.toPredicate()(LabelWrapper("")) }

        expect(true) { spec.toPredicate()(Input().apply { label = "foo" }) }
        expect(false) { spec.toPredicate()(Input().apply { label = "bar" }) }
        expect(false) { spec.toPredicate()(Input()) }
        expect(true) { spec.toPredicate()(TextField("foo")) }
        expect(false) { spec.toPredicate()(TextField("bar")) }
        expect(false) { spec.toPredicate()(TextField()) }
        expect(true) { spec.toPredicate()(TextArea("foo")) }
        expect(false) { spec.toPredicate()(TextArea("bar")) }
        expect(false) { spec.toPredicate()(TextArea()) }
        expect(true) { spec.toPredicate()(TimePicker("foo")) }
        expect(false) { spec.toPredicate()(TimePicker("bar")) }
        expect(false) { spec.toPredicate()(TimePicker()) }
        expect(true) { spec.toPredicate()(DatePicker("foo")) }
        expect(false) { spec.toPredicate()(DatePicker("bar")) }
        expect(false) { spec.toPredicate()(DatePicker()) }
        expect(true) { spec.toPredicate()(ComboBox<Int>("foo")) }
        expect(false) { spec.toPredicate()(ComboBox<Int>("bar")) }
        expect(false) { spec.toPredicate()(ComboBox<Int>()) }
    }

    test("text") {
        val spec = SearchSpec(Component::class.java, text = "foo")
        expect(true) { spec.toPredicate()(Button("foo")) }
        expect(false) { spec.toPredicate()(Button("bar")) }
        expect(false) { spec.toPredicate()(Button()) }
        expect(true) { spec.toPredicate()(Text("foo")) }
        expect(false) { spec.toPredicate()(Text("bar")) }
        expect(false) { spec.toPredicate()(Text("")) }
        expect(false) { spec.toPredicate()(TextField("foo")) }
        expect(false) { spec.toPredicate()(TextField("bar")) }
        expect(false) { spec.toPredicate()(TextField("")) }
    }

    test("predicates") {
        var spec = SearchSpec(Component::class.java).apply {
            predicates.add(Predicate { it is Button })
        }
        expect(true) { spec.toPredicate()(Button()) }
        expect(false) { spec.toPredicate()(NativeLabel()) }
    }
}
