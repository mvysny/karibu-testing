package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.formItem
import com.github.mvysny.karibudsl.v10.textField
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.textfield.TextField
import kotlin.test.expect

@DynaTestDsl
internal fun DynaNodeGroup.formLayoutTest() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("basic FormItem lookup") {
        val f: FormLayout = FormLayout().apply {
            formItem("foo") {
                textField()
            }
        }
        f._expectOne<FormLayout.FormItem> { label = "foo" }
    }

    test("API test: field retrieval from FormItem") {
        lateinit var tf: TextField
        val f: FormLayout = FormLayout().apply {
            formItem("foo") {
                tf = textField()
            }
        }
        expect(tf) { f._get<FormLayout.FormItem> { label = "foo" } .field }
    }

    group("FormItem.field") {
        test("succeeds with one field and no label") {
            val tf = TextField()
            val f: FormLayout.FormItem = FormLayout().addFormItem(tf, "foo")
            expect(tf) { f._get<FormLayout.FormItem> { label = "foo" } .field }
        }
        test("fails with no field") {
            val f: FormLayout.FormItem = FormLayout().addFormItem(Label(), "foo")
            f.removeAll()
            expectThrows(IllegalStateException::class, "FormItem: Expected 1 field but got 0. Component tree:\n└── FormItem[]") {
                f.field
            }
        }
    }
}
