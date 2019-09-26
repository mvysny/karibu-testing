package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.gridpro.GridPro
import elemental.json.Json
import elemental.json.JsonObject

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
