package com.github.mvysny.kaributesting.v10.pro

import com.github.mvysny.kaributesting.v10._fireEvent
import com.vaadin.flow.component.confirmdialog.ConfirmDialog

fun ConfirmDialog._fireConfirm() {
    _fireEvent(ConfirmDialog.ConfirmEvent(this, true))
}

fun ConfirmDialog._fireCancel() {
    _fireEvent(ConfirmDialog.CancelEvent(this, true))
}

fun ConfirmDialog._fireReject() {
    _fireEvent(ConfirmDialog.RejectEvent(this, true))
}
