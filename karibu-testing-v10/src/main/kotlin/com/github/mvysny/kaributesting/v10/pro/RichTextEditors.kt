package com.github.mvysny.kaributesting.v10.pro

import com.github.mvysny.kaributesting.v10._expectEditableByUser
import com.vaadin.flow.component.richtexteditor.RichTextEditor

/**
 * Sets the HTML value of the rich text editor, but only if it is actually possible to do so by the user.
 * If the component is read-only or disabled, an exception is thrown.
 *
 * Also fires value change listeners.
 * @throws IllegalStateException if the field was not visible, not enabled or was read-only.
 */
public fun RichTextEditor._setHtmlValue(value: String) {
    _expectEditableByUser()

    // The implementation doesn't fire value change event directly: it invokes a JavaScript function instead,
    // which sets the HTML snippet client-side, which then causes the component to fire the value-change-event.
    // Therefore, we need to emulate this behavior by firing the value change event manually.
    asHtml().value = value

    // WARNING: Vaadin 14 RichTextEditor clears the HTML value when value change event is fired, then re-reads
    // it by calling RichTextEditor.getHtmlValueString(). We therefore need to make sure that getHtmlValueString() returns the new HTML snippet.
    element.setProperty("htmlValue", value)

    // now we can fire the value change event manually. RichTextEditor.asHtml() captures value-change-event
    // fired on RichTextEditor, modifies it and fires its own event. All we therefore need to do
    // is to fire the value-change-event on the RichTextEditor itself.
    this.value = value
}

public var RichTextEditor._htmlValue: String
    get() = asHtml().value ?: ""
    set(value) {
        _setHtmlValue(value)
    }
