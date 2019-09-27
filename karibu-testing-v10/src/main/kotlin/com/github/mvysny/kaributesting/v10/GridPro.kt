package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.gridpro.EditorType
import com.vaadin.flow.component.gridpro.GridPro
import com.vaadin.flow.component.gridpro.ItemUpdater
import elemental.json.Json
import elemental.json.JsonObject
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Fires the [GridPro.CellEditStartedEvent].
 */
fun <T> GridPro<T>._fireCellEditStartedEvent(item: T, column: Grid.Column<T>,
                                             fromClient: Boolean = true) {
    require(column is GridPro.EditColumn) {
        "The column must be of type GridPro.EditColumn but was ${column.toPrettyString()}"
    }
    val itemJson: JsonObject = Json.createObject()
    itemJson.put("key", dataCommunicator.keyMapper.key(item))
    val event = GridPro.CellEditStartedEvent<T>(this, fromClient,
            itemJson, column._internalId)
    _fireEvent(event)
}


/**
 * Edits given row using the inline [GridPro] editor:
 * ```
 * val person = Person(...)
 * grid._proedit(person) {
 *   nameColumn._text("John")
 *   aliveColumn._checkbox(true)
 * }
 * expect("John") { person.name }
 * expect(true) { person.isAlive }
 * ```
 */
fun <T> Grid<T>._proedit(item: T, editorBlock: GridProMockEditor<T>.() -> Unit) {
    GridProMockEditor(item).apply(editorBlock)
}

/**
 * Mocks [GridPro] row editing capabilities. This class is able to mimic user
 * entering stuff into [column]'s editor for given [item].
 */
class GridProMockEditor<T>(val item: T) {

    private fun Grid.Column<T>.checkEditorColumn() {
        check(this is GridPro.EditColumn) { "${toPrettyString()} needs to be GridPro.EditColumn to use this feature" }
    }

    @Suppress("UNCHECKED_CAST")
    private val Grid.Column<T>.itemUpdater: ItemUpdater<T, String>
        get() {
            checkEditorColumn()
            return itemUpdaterField.get(this) as ItemUpdater<T, String>
        }

    /**
     * The configured editor type for this [column].
     */
    val Grid.Column<T>.editorType: EditorType
        get() {
            checkEditorColumn()
            val editorType = getEditorTypeMethod.invoke(this) as String
            return EditorType.values().first { it.typeName == editorType }
        }

    fun Grid.Column<T>.expectType(expected: EditorType) {
        check(editorType == expected) { "Only applicable to $expected editor type but was $editorType for ${toPrettyString()}" }
    }

    /**
     * Mocks that a text has been entered.
     */
    fun Grid.Column<T>._text(text: String) {
        expectType(EditorType.TEXT)
        itemUpdater.accept(item, text)
    }

    /**
     * Mocks that a checkbox has been checked.
     */
    fun Grid.Column<T>._checkbox(checked: Boolean) {
        expectType(EditorType.CHECKBOX)
        itemUpdater.accept(item, checked.toString())
    }

    /**
     * Flushes the value from the custom field. You should remember the Field instance
     * and set the actual value there:
     * ```
     * val numberField = NumberField()
     * val col = grid.addEditColumn("age").custom(numberField) { p, age -> p.age = age.toInt() }
     * numberField.setValue(3.15)
     * val p = Person("Dedo Jozef", 45)
     * grid._gridproEdit(p) { col._customFlush() }
     * expect(3) { p.age }
     * ```
     */
    fun Grid.Column<T>._customFlush() {
        expectType(EditorType.CUSTOM)
        itemUpdater.accept(item, "") // value doesn't matter, the value will always be read from the Field
    }

    fun Grid.Column<T>._select(option: String) {
        expectType(EditorType.SELECT)
        itemUpdater.accept(item, option)
    }

    fun <E: Enum<E>> Grid.Column<T>._select(e: E) {
        _select(e.toString())
    }

    companion object {
        private val itemUpdaterField: Field = GridPro.EditColumn::class.java.getDeclaredField("itemUpdater").apply { isAccessible = true }
        private val getEditorTypeMethod: Method = GridPro.EditColumn::class.java.getDeclaredMethod("getEditorType").apply { isAccessible = true }
    }
}
