package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.karibudsl.v10.grid
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.data.provider.CallbackDataProvider
import kotlin.test.expect

fun DynaNodeGroup.grid19Testbatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    // https://github.com/mvysny/karibu-testing/issues/72
    test("_get works with FetchCallback") {
        val grid: Grid<String> = UI.getCurrent().grid<String> {
            setItems(CallbackDataProvider.FetchCallback<String, Void> { query ->
                listOf("a", "b", "c").stream().skip(query.offset.toLong()).limit(query.limit.toLong())
            })
        }
        expect("b") { grid._get(1) }
        expect(null) { grid._getOrNull(4) }
    }

    test("dump with FetchCallback") {
        val grid: Grid<String> = UI.getCurrent().grid<String> {
            addColumn { it }
            setItems(CallbackDataProvider.FetchCallback<String, Void> { query ->
                listOf("a", "b", "c").stream().skip(query.offset.toLong()).limit(query.limit.toLong())
            })
        }
        expect("""--[]--
0: a
1: b
2: c
--and possibly more
""") { grid._dump() }
    }
}
