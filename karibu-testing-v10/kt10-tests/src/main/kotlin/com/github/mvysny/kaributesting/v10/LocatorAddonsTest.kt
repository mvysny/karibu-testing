package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.iconButton
import com.github.mvysny.karibudsl.v10.span
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.IronIcon
import com.vaadin.flow.component.icon.VaadinIcon
import kotlin.test.expect

internal fun DynaNodeGroup.locatorAddonsTestbatch() {

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("captionContains") {
        test("fails when caption doesn't match") {
            UI.getCurrent().button("bar")
            expectThrows(AssertionError::class, message = "and captionContains('foo')") {
                _get<Button> { captionContains("foo") }
            }
        }
        test("succeeds when caption matches") {
            UI.getCurrent().add(Button("foo bar"))
            _get<Button> { captionContains("foo") }
        }
        test("picks proper button when caption matches") {
            UI.getCurrent().apply {
                verticalLayout {
                    button("foo bar")
                    button("baz")
                }
            }
            expect("foo bar") { _get<Button> { captionContains("foo") }.caption }
        }
    }

    group("textContains") {
        test("fails when text doesn't match") {
            UI.getCurrent().span("bar")
            expectThrows(AssertionError::class, message = "and textContains('foo')") {
                _get<Span> { textContains("foo") }
            }
        }
        test("succeeds when text matches") {
            UI.getCurrent().span("foo bar")
            _get<Span> { textContains("foo") }
        }
        test("picks proper span when text matches") {
            UI.getCurrent().apply {
                verticalLayout {
                    span("foo bar")
                    span("baz")
                }
            }
            expect("foo bar") { _get<Span> { textContains("foo") }.text }
        }
    }
    group("buttonIconIs") {
        test("smoke") {
            UI.getCurrent().button("iconless")
            val btn: Button = UI.getCurrent().iconButton(VaadinIcon.VAADIN_H.create())
            UI.getCurrent().iconButton(VaadinIcon.HOURGLASS.create())
            expect(btn) { _get<Button> { buttonIconIs(VaadinIcon.VAADIN_H) } }
        }
    }
    group("IconName") {
        test("smoke") {
            expect(IconName("vaadin", "bar")) {
                Icon("bar").iconName
            }
            expect(IconName("foo", "bar")) {
                IronIcon("foo", "bar").iconName
            }
        }

        test("vaadin icon") {
            expect(VaadinIcon.HOURGLASS_END) { IconName.of(VaadinIcon.HOURGLASS_END).asVaadinIcon() }
            expect(VaadinIcon.ABACUS) { IconName.of(VaadinIcon.ABACUS).asVaadinIcon() }
            expect(VaadinIcon.LIST_OL) {
                VaadinIcon.LIST_OL.create().iconName!!.asVaadinIcon()
            }
        }

        test("changing icon") {
            val icon = VaadinIcon.ABACUS.create()
            icon.iconName = IconName.of(VaadinIcon.VAADIN_H)
            expect(VaadinIcon.VAADIN_H) { icon.iconName!!.asVaadinIcon() }
        }

        test("clearing icon") {
            val icon = VaadinIcon.ABACUS.create()
            icon.iconName = null
            expect(null) { icon.iconName }
        }
    }
}
