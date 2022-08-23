package com.github.mvysny.kaributesting.v10.groovy.pro

import com.github.mvysny.kaributesting.v10.pro.ConfirmDialogKt
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

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

    @Nullable
    static String getText(@NotNull ConfirmDialog self) {
        ConfirmDialogKt.getText(self)
    }

    @NotNull
    static List<Component> getTextComponents(@NotNull ConfirmDialog self) {
        ConfirmDialogKt.getTextComponents(self)
    }

    @Nullable
    static String getHeader(@NotNull ConfirmDialog self) {
        ConfirmDialogKt.getHeader(self)
    }

    @NotNull
    static List<Component> getHeaderComponents(@NotNull ConfirmDialog self) {
        ConfirmDialogKt.getHeaderComponents(self)
    }

    /**
     * Returns the text set via {@link ConfirmDialog#setText(java.lang.String)}.
     * If that's null then retrieves recursive text contents of all {@link #getTextComponents(com.vaadin.flow.component.confirmdialog.ConfirmDialog)}.
     */
    @NotNull
    static String getTextRecursively(@NotNull ConfirmDialog self) {
        ConfirmDialogKt.getTextRecursively(self)
    }

    /**
     * Returns the text set via {@link ConfirmDialog#setHeader(java.lang.String)};
     * if that's null then retrieves recursive text contents of all {@link #getHeaderComponents(com.vaadin.flow.component.confirmdialog.ConfirmDialog)}.
     */
    @NotNull
    static String getHeaderText(@NotNull ConfirmDialog self) {
        ConfirmDialogKt.getHeaderText(self)
    }
}
