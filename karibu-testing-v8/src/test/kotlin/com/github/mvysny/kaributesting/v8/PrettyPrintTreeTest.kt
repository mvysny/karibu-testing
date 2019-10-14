package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.karibudsl.v8.label
import com.vaadin.icons.VaadinIcons
import com.vaadin.server.*
import com.vaadin.ui.*
import java.io.File
import kotlin.test.expect
import kotlin.test.fail

class PrettyPrintTreeTest : DynaTest({
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("Simple dump") {
        val div = VerticalLayout().apply {
            label("Foo")
        }
        expect("""
└── VerticalLayout[]
    └── Label[value='Foo']
""".trim()) { div.toPrettyTree().trim() }
    }

    group("toPrettyString()") {
        test("component") {
            expect("Label[value='foo']") { Label("foo").toPrettyString() }
            expect("VerticalLayout[INVIS]") { VerticalLayout().apply { isVisible = false }.toPrettyString() }
            expect("TextField[#25, value='']") { TextField().apply { id = "25" }.toPrettyString() }
            expect("Button[caption='click me']") { Button("click me").toPrettyString() }
            expect("TextArea[caption='label', value='some text']") { TextArea("label").apply { value = "some text" }.toPrettyString() }
            expect("Grid[columns=[]]") { Grid<Any>().toPrettyString() }
            expect("Grid[caption='cap', columns=['My Header']]") { Grid<Any>("cap").apply { addColumn { it }.apply { caption = "My Header" } }.toPrettyString() }
            expect("Link[href='']") { Link().toPrettyString() }
            expect("Link[caption='foo', href='vaadin.com']") { Link("foo", ExternalResource("vaadin.com")).toPrettyString() }
            expect("Image[src='']") { Image().toPrettyString() }
            expect("Image[caption='bar', src='vaadin.com']") { Image("bar", ExternalResource("vaadin.com")).toPrettyString() }
            expect("TextField[#25, value='', componentError='failed validation']") { TextField().apply { id = "25"; componentError = UserError("failed validation") }.toPrettyString() }
        }
        test("resource") {
            expect("vaadin.com") { ExternalResource("vaadin.com").toPrettyString() }
            expect("VaadinIcons[ABACUS]") { VaadinIcons.ABACUS.toPrettyString() }
            expect("ThemeResource[kvak]") { ThemeResource("kvak").toPrettyString() }
            expect("StreamResource[kvak.png]") { StreamResource({ null }, "kvak.png").toPrettyString() }
            expect("ClassResource[com.github.mvysny.kaributesting.v8.MockUI/foo.png]") { ClassResource("foo.png").toPrettyString() }
            expect("FileResource[/tmp/image.png]") { FileResource(File("/tmp/image.png")).toPrettyString() }
            expect("FontAwesome[ADJUST]") { FontAwesome.ADJUST.toPrettyString() }
        }
    }
})
