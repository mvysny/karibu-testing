package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.grid
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.data.provider.CallbackDataProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractGrid19Tests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class `FetchCallback with no size info` {
        @Nested inner class _get {
            // https://github.com/mvysny/karibu-testing/issues/72
            @Test fun `offset-limit`() {
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
            @Test fun paging() {
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

        @Nested inner class _dump {
            @Test fun `3 items`() {
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
            @Test fun `30 items`() {
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

        @Test fun _expectRows() {
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
