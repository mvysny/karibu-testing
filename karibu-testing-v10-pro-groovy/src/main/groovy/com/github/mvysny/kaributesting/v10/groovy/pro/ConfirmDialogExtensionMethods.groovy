package com.github.mvysny.kaributesting.v10.groovy.pro

import com.github.mvysny.kaributesting.v10.pro.ConfirmDialogKt
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

@CompileStatic
class ConfirmDialogExtensionMethods {
    static void _fireConfirm(@NotNull ConfirmDialog self) {
        ConfirmDialogKt._fireConfirm(self)
    }

    static void _fireCancel(@NotNull ConfirmDialog self) {
        ConfirmDialogKt._fireCancel(self)
    }

    static void _fireReject(@NotNull ConfirmDialog self) {
        ConfirmDialogKt._fireReject(self)
    }

    static String getText(@NotNull ConfirmDialog self) {
        ConfirmDialogKt.getText(self)
    }

    static String getTextComponents(@NotNull ConfirmDialog self) {
        ConfirmDialogKt.getTextComponents(self)
    }

    static String getHeader(@NotNull ConfirmDialog self) {
        ConfirmDialogKt.getHeader(self)
    }

    static String getHeaderComponents(@NotNull ConfirmDialog self) {
        ConfirmDialogKt.getHeaderComponents(self)
    }
}
