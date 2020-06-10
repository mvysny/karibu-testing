package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.GridKt
import com.github.mvysny.kaributesting.v10.PrettyPrintTree
import com.github.mvysny.kaributesting.v10.RenderersKt
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.grid.FooterRow
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.HeaderRow
import com.vaadin.flow.component.treegrid.TreeGrid
import com.vaadin.flow.data.provider.DataCommunicator
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.provider.QuerySortOrder
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider
import com.vaadin.flow.data.renderer.BasicRenderer
import com.vaadin.flow.data.renderer.Renderer
import com.vaadin.flow.data.renderer.TemplateRenderer
import com.vaadin.flow.function.SerializablePredicate
import com.vaadin.flow.function.ValueProvider
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam
import groovy.transform.stc.SimpleType
import kotlin.Unit
import kotlin.jvm.functions.Function1
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import static org.junit.jupiter.api.Assertions.fail

/**
 * @author mavi
 */
@CompileStatic
class GridExtensionMethods {
    /**
     * Returns the item on given row. Fails if the row index is invalid. The data provider is
     * sorted according to given [sortOrders] (empty by default) and filtered according
     * to given [filter] (null by default) first.
     * @param rowIndex the row, 0..size - 1
     * @return the item at given row.
     * @throws AssertionError if the row index is out of bounds.
     */
    @Nullable
    static <T, F> T _get(@NotNull DataProvider<T, F> self,
                       int rowIndex,
                       @NotNull List<? extends QuerySortOrder> sortOrders = [],
                       @Nullable Comparator<T> inMemorySorting = null,
                       @Nullable F filter = null) {
        return GridKt._get(self, rowIndex, sortOrders, inMemorySorting, filter)
    }

    /**
     * Returns all items in given data provider, sorted according to given [sortOrders] (empty by default) and filtered according
     * to given [filter] (null by default).
     * @return the list of items.
     */
    @NotNull
    static <T, F> List<T> _findAll(@NotNull DataProvider<T, F> self,
                                   @NotNull List<? extends QuerySortOrder> sortOrders = [],
                                   @Nullable Comparator<T> inMemorySorting = null,
                                   @Nullable F filter = null) {
        return GridKt._findAll(self, sortOrders, inMemorySorting, filter)
    }

    /**
     * Returns the item on given row. Fails if the row index is invalid. Uses current Grid sorting.
     * <p></p>
     * For [TreeGrid] this returns the x-th displayed row; skips children of collapsed nodes.
     * Uses [_rowSequence].
     * <p></p>
     * WARNING: Very slow operation for [TreeGrid].
     * @param rowIndex the row, 0..size - 1
     * @return the item at given row, not null.
     */
    @Nullable
    static <T> T _get(@NotNull Grid<T> self, int rowIndex) {
        return GridKt._get(self, rowIndex)
    }

    /**
     * For [TreeGrid] this walks the [_rowSequence].
     * <p></p>
     * WARNING: Very slow operation for [TreeGrid].
     */
    @NotNull
    static <T> List<T> _fetch(@NotNull Grid<T> self, int offset, int limit) {
        return GridKt._fetch(self, offset, limit)
    }

    @NotNull
    static <T> List<T> fetch(@NotNull DataCommunicator<T> self, int offset, int limit) {
        return GridKt.fetch(self, offset, limit)
    }

    /**
     * Returns all items in given data provider. Uses current Grid sorting.
     * <p></p>
     * For [TreeGrid] this returns all displayed rows; skips children of collapsed nodes.
     * @return the list of items.
     */
    @NotNull
    static <T> List<T> _findAll(@NotNull Grid<T> self) {
        return GridKt._findAll(self)
    }

    /**
     * Returns the number of items in this data provider.
     * <p></p>
     * In case of [HierarchicalDataProvider]
     * this returns the number of ALL items including all leafs.
     */
    static <T, F> int _size(@NotNull DataProvider<T, F> self, @Nullable F filter = null) {
        return GridKt._size(self, filter)
    }

    /**
     * Returns the number of items in this data provider, including child items.
     * The function traverses recursively until all children are found; then a total size
     * is returned. The function uses [HierarchicalDataProvider.size] mostly, but
     * also uses [HierarchicalDataProvider.fetchChildren] to discover children.
     * Only children matching [filter] are considered for recursive computation of
     * the size.
     * <p></p>
     * Note that this can differ to `Grid._size()` since `Grid._size()` ignores children
     * of collapsed tree nodes.
     * @param root start with this item; defaults to null to iterate all items
     * @param filter filter to pass to [HierarchicalQuery]
     */
    static <T, F> int _size(@NotNull HierarchicalDataProvider<T, F> self,
                            @Nullable T root = null,
                            @Nullable F filter = null) {
        return GridKt._size(self, root, filter)
    }

    /**
     * Returns the number of items in this Grid.
     * <p></p>
     * For [TreeGrid] this computes the number of items the [TreeGrid] is actually showing on-screen,
     * ignoring children of collapsed nodes.
     * <p></p>
     * A very slow operation for [TreeGrid] since it walks through all items returned by [_rowSequence].
     */
    static int _size(@NotNull Grid<?> self) {
        return GridKt._size(self)
    }

    /**
     * Gets a [Grid.Column] of this grid by its [columnKey].
     * @throws AssertionError if no such column exists.
     */
    @NotNull
    static <T> Grid.Column<T> _getColumnByKey(@NotNull Grid<T> self, @NotNull String columnKey) {
        return GridKt._getColumnByKey(self, columnKey)
    }

    /**
     * Performs a click on a [ClickableRenderer] in given [Grid] cell. Only supports the following scenarios:
     * * [ClickableRenderer]
     * * [ComponentRenderer] which renders a [Button]
     * * [ComponentRenderer] which renders something else than a [Button]; then you need to provide the [click] closure which can click on such a component.
     * @param rowIndex the row index, 0 or higher.
     * @param columnKey the column key [Grid.Column.getKey]
     * @param click if [ComponentRenderer] doesn't produce a button, this is called, to click the component returned by the [ComponentRenderer]
     * @throws IllegalStateException if the renderer is not [ClickableRenderer] nor [ComponentRenderer].
     */
    static <T> void _clickRenderer(
            @NotNull Grid<T> self, int rowIndex, @NotNull String columnKey,
            @NotNull @ClosureParams(value = SimpleType, options = "com.vaadin.flow.component.Component") Closure click = { Component component ->
                fail("${PrettyPrintTreeUtils.toPrettyString(self)} column $columnKey: ClickableRenderer produced ${PrettyPrintTreeUtils.toPrettyString(component)} which is not a button - you need to provide your own custom 'click' closure which knows how to click this component")
            }
    ) {
        GridKt._clickRenderer(self, rowIndex, columnKey, new Function1<Component, Unit>() {
            @Override
            Unit invoke(Component component) {
                click(component)
                return Unit.INSTANCE
            }
        })
    }

    /**
     * Returns the formatted value as a String. Does not use renderer to render the value - simply calls value provider and presentation provider
     * and converts the result to string (even if the result is a [Component]).
     * @param rowIndex the row index, 0 or higher.
     * @param columnId the column ID.
     */
    @NotNull
    static <T> String _getFormatted(@NotNull Grid<T> self, int rowIndex, @NotNull String columnKey) {
        GridKt._getFormatted(self, rowIndex, columnKey)
    }

    /**
     * Returns the formatted value as a String. Does not use renderer to render the value - simply calls value provider and presentation provider
     * and converts the result to string (even if the result is a [Component]).
     * @param rowIndex the row index, 0 or higher.
     */
    @NotNull
    static <T> String _getFormatted(@NotNull Grid.Column<T> self, @NotNull T rowObject) {
        GridKt._getFormatted(self, rowObject)
    }

    @NotNull
    static <T> List<String> _getFormattedRow(@NotNull Grid<T> self, @NotNull T rowObject) {
        return GridKt._getFormattedRow(self, rowObject)
    }

    @NotNull
    static <T> List<String> _getFormattedRow(@NotNull Grid<T> self, int rowIndex) {
        GridKt._getFormattedRow(self, rowIndex)
    }

    @Nullable
    static <T> Object getPresentationValue(@NotNull Grid.Column<T> self, @NotNull T rowObject) {
        GridKt.getPresentationValue(self, rowObject)
    }

    @NotNull
    static <T, V> ValueProvider<T, V> getValueProvider2(@NotNull BasicRenderer<T, V> self) {
        RenderersKt.getValueProvider(self)
    }

    /**
     * Renders the template for given [item]
     */
    @NotNull
    static <T> String renderTemplate(@NotNull TemplateRenderer<T> self, @NotNull T item) {
        RenderersKt.renderTemplate(self, item)
    }

    @NotNull
    static String getTemplate(@NotNull Renderer<?> self) {
        RenderersKt.getTemplate(self)
    }

    /**
     * Sets and retrieves the column header as set by [Grid.Column.setHeader] (String). The result value is undefined if a component has been set as the header.
     */
    @NotNull
    static <T> String getHeader2(@NotNull Grid.Column<T> self) {
        GridKt.getHeader2(self)
    }

    /**
     * Dumps given range of [rows] of the Grid, formatting the values using the [_getFormatted] function. The output example:
     * <pre>
     * --[Name]--[Age]--[Occupation]--
     * 0: John, 25, Service Worker
     * 1: Fred, 40, Supervisor
     * --and 198 more
     * </pre>
     */
    static <T> String _dump(@NotNull Grid<T> self, @NotNull IntRange rows = 0..10) {
        GridKt._dump(self, new kotlin.ranges.IntRange(rows.getFrom(), rows.getTo()))
    }

    static void expectRows(@NotNull Grid<?> self, int count) {
        GridKt.expectRows(self, count)
    }

    static void expectRow(@NotNull Grid<?> self, int rowIndex, @NotNull String... row) {
        GridKt.expectRow(self, rowIndex, row)
    }

    /**
     * Retrieves the cell for given [Grid.Column.getKey].
     * @return the corresponding cell
     * @throws IllegalArgumentException if no such column exists.
     */
    @NotNull
    static HeaderRow.HeaderCell getCell(@NotNull HeaderRow self, @NotNull String key) {
        GridKt.getCell(self, key)
    }

    /**
     * Retrieves the cell for given [property]; it matches [Grid.Column.getKey] to [KProperty1.name].
     * @return the corresponding cell
     * @throws IllegalArgumentException if no such column exists.
     */
    @NotNull
    static FooterRow.FooterCell getCell(@NotNull FooterRow self, @NotNull String key) {
        GridKt.getCell(self, key)
    }

    @Nullable
    static Renderer<?> getRenderer(@NotNull HeaderRow.HeaderCell self) {
        GridKt.getRenderer(self)
    }

    @Nullable
    static Renderer<?> getRenderer(@NotNull FooterRow.FooterCell self) {
        GridKt.getRenderer(self)
    }

    /**
     * Returns or sets the component in grid's footer cell. Returns `null` if the cell contains String, something else than a component or nothing at all.
     */
    @Nullable
    static Component getComponent(@NotNull FooterRow.FooterCell self) {
        GridKt.getComponent(self)
    }

    /**
     * Returns or sets the component in grid's header cell. Returns `null` if the cell contains String, something else than a component or nothing at all.
     */
    @Nullable
    static Component getComponent(@NotNull HeaderRow.HeaderCell self) {
        GridKt.getComponent(self)
    }

    @NotNull
    static QuerySortOrder getAsc(@NotNull String sorted) {
        new QuerySortOrder(sorted, SortDirection.ASCENDING)
    }
    @NotNull
    static QuerySortOrder getDesc(@NotNull String sorted) {
        new QuerySortOrder(sorted, SortDirection.DESCENDING)
    }

    /**
     * Sorts given grid. Affects [_findAll], [_get] and other data-fetching functions.
     */
    static <T> void sort(@NotNull Grid<T> self, QuerySortOrder... sortOrder) {
        GridKt.sort(self, sortOrder)
    }

    /**
     * Fires the [ItemClickEvent] event for given [rowIndex] which invokes all item click listeners registered via
     * [Grid.addItemClickListener].
     * @param button the id of the pressed mouse button
     * @param ctrlKey `true` if the control key was down when the event was fired, `false` otherwise
     * @param shiftKey `true` if the shift key was down when the event was fired, `false` otherwise
     * @param altKey `true` if the alt key was down when the event was fired, `false` otherwise
     * @param metaKey `true` if the meta key was down when the event was fired, `false` otherwise
     */
    static <T> void _clickItem(@NotNull Grid<T> self, int rowIndex, int button = 1, boolean ctrlKey = false,
            boolean shiftKey = false, boolean altKey = false, boolean metaKey = false) {
        GridKt._clickItem(self, rowIndex, button, ctrlKey, shiftKey, altKey, metaKey)
    }

    /**
     * Returns a sequence which walks over all rows the [TreeGrid] is actually showing.
     * The sequence will *skip* children of collapsed nodes.
     * <p></p>
     * Iterating the entire sequence is a very slow operation since it will repeatedly
     * poll [HierarchicalDataProvider] for list of children.
     * <p></p>
     * Honors current grid ordering.
     */
    static <T> Iterator<T> _rowSequence(@NotNull TreeGrid<T> self,
                                        @Nullable @ClosureParams(FirstParam.FirstGenericType.class) Closure<Boolean> filter = null) {
        SerializablePredicate<T> convertedFilter = filter == null ? null : new SerializablePredicate<T>() {
            @Override
            boolean test(T t) {
                return filter(t)
            }
        }
        def sequence = GridKt._rowSequence(self, convertedFilter)
        sequence.iterator()
    }

    /**
     * Returns the number of items the [TreeGrid] is actually showing. For example
     * it doesn't count in children of collapsed nodes.
     *
     * A very slow operation since it walks through all items returned by [_rowSequence].
     */
    static int _size(@NotNull TreeGrid<?> self) {
        GridKt._size(self)
    }

    @NotNull
    static PrettyPrintTree _dataSourceToPrettyTree(@NotNull TreeGrid<?> self) {
        GridKt._dataSourceToPrettyTree(self as TreeGrid)
    }

    @NotNull
    static <T> List<T> _getRootItems(@NotNull TreeGrid<T> self) {
        GridKt._getRootItems(self)
    }

    /**
     * Expands all nodes. May invoke massive data loading.
     */
    static void _expandAll(@NotNull TreeGrid<?> self, int depth = 100) {
        GridKt._expandAll(self as TreeGrid, depth)
    }

    /**
     * Returns the column's Internal ID.
     */
    @NotNull
    static String get_internalId(@NotNull Grid.Column<?> self) {
        GridKt.get_internalId(self)
    }
}
