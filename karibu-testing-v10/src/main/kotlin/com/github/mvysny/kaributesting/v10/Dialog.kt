import com.vaadin.flow.component.Component
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.dom.Element

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
private fun getChildrenOfDialogHeaderOrFooter(dialogHeaderFooter: Any): List<Component> {
    val root = dialogHeaderFooter_rootField.get(dialogHeaderFooter) as Element
    return root.children.map { it.component.orElse(null) } .toList().filterNotNull()
}

/**
 * Vaadin 23.1 added Dialog.footer and header
 */
public fun Dialog._getDialogHeaderChildren(): List<Component> {
    return getChildrenOfDialogHeaderOrFooter(this.header)
}
/**
 * Vaadin 23.1 added Dialog.footer and header
 */
public fun Dialog._getDialogFooterChildren(): List<Component> {
    return getChildrenOfDialogHeaderOrFooter(footer)
}
