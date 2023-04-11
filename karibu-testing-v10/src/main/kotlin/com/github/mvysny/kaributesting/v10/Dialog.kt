package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.VaadinVersion
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.dom.Element
import kotlin.streams.toList

/**
 * Vaadin 23.1 added Dialog.footer and header
 */
private val dialogHeaderFooter_rootField by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val f =
        Class.forName("com.vaadin.flow.component.dialog.Dialog${'$'}DialogHeaderFooter")
            .getDeclaredField("root")
    f.isAccessible = true
    f
}
/**
 * Vaadin 23.1 added Dialog.footer and header
 */
private val dialog_getHeader by lazy(LazyThreadSafetyMode.PUBLICATION) {
    Dialog::class.java.getDeclaredMethod("getHeader")
}

/**
 * Vaadin 23.1 added Dialog.footer and header
 */
private val dialog_getFooter by lazy(LazyThreadSafetyMode.PUBLICATION) {
    Dialog::class.java.getDeclaredMethod("getFooter")
}

/**
 * Vaadin 23.1 added Dialog.footer and header
 */
private fun getChildrenOfDialogHeaderOrFooter(dialogHeaderFooter: Any): List<Component> {
    val root = dialogHeaderFooter_rootField.get(dialogHeaderFooter) as Element
    return root.children.map { it.component.orElse(null) } .toList().filterNotNull()
}

/**
 * Vaadin 23.1 added Dialog.footer and header
 */
public fun Dialog._getDialogHeaderChildren(): List<Component> {
    if (!VaadinVersion.get.isAtLeast(23, 1)) {
        return listOf()
    }
    val header = dialog_getHeader.invoke(this)
    return getChildrenOfDialogHeaderOrFooter(header)
}
/**
 * Vaadin 23.1 added Dialog.footer and header
 */
public fun Dialog._getDialogFooterChildren(): List<Component> {
    if (!VaadinVersion.get.isAtLeast(23, 1)) {
        return listOf()
    }
    val header = dialog_getFooter.invoke(this)
    return getChildrenOfDialogHeaderOrFooter(header)
}
