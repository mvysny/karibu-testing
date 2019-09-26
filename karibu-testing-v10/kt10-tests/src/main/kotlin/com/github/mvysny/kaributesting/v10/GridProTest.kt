package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.vaadin.flow.component.gridpro.GridPro
import kotlin.test.expect

internal fun DynaNodeGroup.gridProTestbatch() {

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    if (VaadinMeta.version >= 14) {
        // Only GridPro 2.0.0 has addCellEditStartedListener()
        test("CellEditStartedEvent") {
            val grid = GridPro<TestPerson>(TestPerson::class.java)
            grid.removeAllColumns()
            val col = grid.addEditColumn("age")
            var listenerCalled = false
            grid.addCellEditStartedListener {
                listenerCalled = true
                expect(25) { it.item.age }
                expect(true) { it.isFromClient }
            }
            grid._fireCellEditStartedEvent(TestPerson("foo", 25), col.column)
            expect(true) { listenerCalled }
        }
    }
}
