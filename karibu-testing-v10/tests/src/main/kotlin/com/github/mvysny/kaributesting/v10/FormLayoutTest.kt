package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.formItem
import com.github.mvysny.karibudsl.v10.textField
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.TextField
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractFormLayoutTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun `basic FormItem lookup`() {
        val f: FormLayout = FormLayout().apply {
            formItem("foo") {
                textField()
            }
        }
        f._expectOne<FormLayout.FormItem> { label = "foo" }
    }

    @Test fun `API test - field retrieval from FormItem`() {
        lateinit var tf: TextField
        val f: FormLayout = FormLayout().apply {
            formItem("foo") {
                tf = textField()
            }
        }
        expect(tf) { f._get<FormLayout.FormItem> { label = "foo" } .field }
    }

    @Nested inner class formitem {
        @Test
        fun `lookup-of-component-from-within-formitem`() {
            val tf = TextField()
            val fl = FormLayout()
            val f: FormLayout.FormItem = fl.addFormItem(tf, "foo")
            expect(tf) { f._get<TextField>() }
            expect(tf) { fl._get<TextField>() }
        }

        /**
         * Tests [field].
         */
        @Nested inner class `FormItem-field` {
            @Test
            fun `succeeds with one field and no label`() {
                val tf = TextField()
                val fl = FormLayout()
                val f: FormLayout.FormItem = fl.addFormItem(tf, "foo")
                expect(tf) {
                    f._get<FormLayout.FormItem> {
                        label = "foo"
                    }.field
                }
                expect(tf) {
                    fl._get<FormLayout.FormItem> {
                        label = "foo"
                    }.field
                }
            }

            @Test
            fun `fails with no field`() {
                val f: FormLayout.FormItem =
                    FormLayout().addFormItem(Select<String>(), "foo")
                f.removeAll()
                expectThrows(
                    IllegalStateException::class,
                    "FormItem: Expected 1 field but got 0. Component tree:\n└── FormItem[]"
                ) {
                    f.field
                }
            }
        }
    }

    /**
     * New stuff in Vaadin 24.8t
     */
    @Nested inner class formrow {
        @Test fun `lookup`() {
            val tf = TextField()
            val fl = FormLayout()
            val f: FormLayout.FormRow = fl.addFormRow(tf)
            expect(tf) { f._get<TextField>() }
            expect(tf) { fl._get<TextField>() }
        }
        @Test fun `fails with no field`() {
            val fl = FormLayout()
            val f: FormLayout.FormRow = fl.addFormRow(TextField())
            f.removeAll()
            f._expectNone<TextField>()
            fl._expectNone<TextField>()
        }
    }
}
