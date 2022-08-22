package com.github.mvysny.kaributesting.v10.pro

import com.github.mvysny.kaributesting.v10._fireEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import kotlin.streams.toList

public fun ConfirmDialog._fireConfirm() {
    _fireEvent(ConfirmDialog.ConfirmEvent(this, true))
    close() // close the ConfirmDialog afterwards, to emulate the ConfirmDialog behavior: https://github.com/mvysny/karibu-testing/issues/34
}

public fun ConfirmDialog._fireCancel() {
    _fireEvent(ConfirmDialog.CancelEvent(this, true))
    close() // close the ConfirmDialog afterwards, to emulate the ConfirmDialog behavior: https://github.com/mvysny/karibu-testing/issues/34
}

public fun ConfirmDialog._fireReject() {
    _fireEvent(ConfirmDialog.RejectEvent(this, true))
    close() // close the ConfirmDialog afterwards, to emulate the ConfirmDialog behavior: https://github.com/mvysny/karibu-testing/issues/34
}

/**
 * Returns the text set via [setText(String)][ConfirmDialog.setText].
 */
public fun ConfirmDialog.getText(): String? = element.getProperty("message")

/**
 * Returns all text components set via [setText(Component)][ConfirmDialog.setText].
 */
public fun ConfirmDialog.getTextComponents(): List<Component> {
    val textElements = element.children.filter { !it.hasAttribute("slot") }.toList()
    return textElements.mapNotNull { it.component.orElse(null) }
}

/**
 * Returns the text set via [setHeader(String)][ConfirmDialog.setHeader].
 */
public fun ConfirmDialog.getHeader(): String? = element.getProperty("header")

/**
 * Returns all header components set via [setHeader(Component)][ConfirmDialog.setHeader].
 */
public fun ConfirmDialog.getHeaderComponents(): List<Component> {
    val textElements = element.children.filter { it.getAttribute("slot") == "header" }.toList()
    return textElements.mapNotNull { it.component.orElse(null) }
}
