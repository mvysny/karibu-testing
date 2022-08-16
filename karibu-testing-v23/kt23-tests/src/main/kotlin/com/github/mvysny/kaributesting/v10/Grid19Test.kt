package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.grid
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.data.provider.CallbackDataProvider
import kotlin.test.expect

@DynaTestDsl
fun DynaNodeGroup.grid19Testbatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("FetchCallback with no size info") {
        group("_get") {
            // https://github.com/mvysny/karibu-testing/issues/72
            test("offset/limit") {
                val grid: Grid<String> = UI.getCurrent().grid<String> {
                    setItems(CallbackDataProvider.FetchCallback<String, Void> { query ->
                        listOf("a", "b", "c").stream()
                            .skip(query.offset.toLong())
                            .limit(query.limit.toLong())
                    })
                }
                expect("b") { grid._get(1) }
                expect(null) { grid._getOrNull(4) }
            }
            test("paging") {
                // https://github.com/mvysny/karibu-testing/issues/99
                val grid: Grid<Int> = UI.getCurrent().grid<Int> {
                    setItems(CallbackDataProvider.FetchCallback<Int, Void> { query ->
                        (0..1000).toList().stream()
                            .skip(query.page.toLong() * query.pageSize)
                            .limit(query.pageSize.toLong())
                    })
                }
                expect(0) { grid._get(0) }
                expect(1) { grid._get(1) }
                expect(1000) { grid._get(1000) }
                expect(null) { grid._getOrNull(1001) }
            }
        }

        group("_dump") {
            test("3 items") {
                val grid: Grid<String> = UI.getCurrent().grid<String> {
                    addColumn { it }
                    setItems(CallbackDataProvider.FetchCallback<String, Void> { query ->
                        listOf("a", "b", "c").stream()
                            .skip(query.offset.toLong())
                            .limit(query.limit.toLong())
                    })
                    _prepare()
                }
                expect(
                    """Grid[<String>, dataprovider='CallbackDataProvider']
--[]--
0: a
1: b
2: c
--
"""
                ) { grid._dump() }
            }
            test("30 items") {
                val grid: Grid<String> = UI.getCurrent().grid<String> {
                    addColumn { it }
                    setItems(CallbackDataProvider.FetchCallback<String, Void> { query ->
                        (0..29).map { it.toString() }.stream()
                            .skip(query.offset.toLong())
                            .limit(query.limit.toLong())
                    })
                    _prepare()
                }
                expect(
                    """Grid[<String>, dataprovider='CallbackDataProvider']
--[]--
0: 0
1: 1
2: 2
3: 3
4: 4
5: 5
6: 6
7: 7
8: 8
9: 9
--and possibly more
"""
                ) { grid._dump() }
            }
        }

        test("_expectRows") {
            val grid: Grid<String> = UI.getCurrent().grid<String> {
                setItems(CallbackDataProvider.FetchCallback<String, Void> { query ->
                    (0..29).map { it.toString() }.stream()
                        .skip(query.offset.toLong())
                        .limit(query.limit.toLong())
                })
            }
            grid.expectRows(30) // should succeed
            expectThrows(AssertionError::class, "expected 29 rows but got 30") {
                grid.expectRows(29) // should fail
            }
        }
    }
}
