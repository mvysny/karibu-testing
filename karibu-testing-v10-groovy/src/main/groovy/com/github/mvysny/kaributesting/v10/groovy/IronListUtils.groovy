package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.GridKt
import com.github.mvysny.kaributesting.v10.IronListKt
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.ironlist.IronList
import com.vaadin.flow.data.renderer.Renderer
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * @author Martin Vysny <mavi@vaadin.com>
 */
@CompileStatic
class IronListUtils {
    /**
     * Returns the item on given row. Fails if the row index is invalid.
     * @param rowIndex the row, 0..size - 1
     * @return the item at given row.
     * @throws AssertionError if the row index is out of bounds.
     */
    @Nullable
    static <T> T _get(@NotNull IronList<T> self,
                      int rowIndex) {
        return IronListKt._get(self, rowIndex)
    }

    /**
     * Fetches items from [IronList]'s data provider.
     */
    @NotNull
    static <T> List<T> _fetch(@NotNull IronList<T> self, int offset, int limit) {
        return IronListKt._fetch(self, offset, limit)
    }

    /**
     * Returns all items in given data provider.
     * @return the list of items.
     */
    @NotNull
    static <T> List<T> _findAll(@NotNull IronList<T> self) {
        return IronListKt._findAll(self)
    }

    /**
     * Returns the number of items in this IronList.
     */
    static int _size(@NotNull IronList<?> self) {
        return IronListKt._size(self)
    }

    @NotNull
    static <T> Renderer<T> get_renderer(@NotNull IronList<T> self) {
        return IronListKt.get_renderer(self)
    }

    /**
     * Returns the formatted value as a String. Does not use renderer to render the value - simply calls value provider and presentation provider
     * and converts the result to string (even if the result is a [Component]).
     * @param rowIndex the row index, 0 or higher.
     * @param columnId the column ID.
     */
    @NotNull
    static <T> String _getFormattedRow(@NotNull IronList<T> self, int rowIndex) {
        return IronListKt._getFormattedRow(self, rowIndex)
    }

    /**
     * Dumps given range of [rows] of the IronList, formatting the values using the [_getFormattedRow] function. The output example:
     * ```
     * ----------------------
     * 0: John
     * 1: Fred
     * --and 198 more
     * ```
     */
    @NotNull
    static <T> String _dump(@NotNull IronList<T> self, @NotNull IntRange rows = 0..10) {
        IronListKt._dump(self, new kotlin.ranges.IntRange(rows.getFrom(), rows.getTo()))
    }

    static void expectRows(@NotNull IronList<?> self, int count) {
        IronListKt.expectRows(self, count)
    }

    static void expectRow(@NotNull IronList<?> self, int rowIndex, @NotNull String row) {
        IronListKt.expectRow(self, rowIndex, row)
    }
}
