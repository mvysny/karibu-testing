package com.github.mvysny.kaributesting.v10.pro

import com.github.mvysny.kaributesting.v10._fireEvent
import com.vaadin.flow.component.confirmdialog.ConfirmDialog

fun ConfirmDialog._fireConfirm() {
    _fireEvent(ConfirmDialog.ConfirmEvent(this, true))
    close() // close the ConfirmDialog afterwards, to emulate the ConfirmDialog behavior: https://github.com/mvysny/karibu-testing/issues/34
}

fun ConfirmDialog._fireCancel() {
    _fireEvent(ConfirmDialog.CancelEvent(this, true))
    close() // close the ConfirmDialog afterwards, to emulate the ConfirmDialog behavior: https://github.com/mvysny/karibu-testing/issues/34
}

fun ConfirmDialog._fireReject() {
    _fireEvent(ConfirmDialog.RejectEvent(this, true))
    close() // close the ConfirmDialog afterwards, to emulate the ConfirmDialog behavior: https://github.com/mvysny/karibu-testing/issues/34
}
