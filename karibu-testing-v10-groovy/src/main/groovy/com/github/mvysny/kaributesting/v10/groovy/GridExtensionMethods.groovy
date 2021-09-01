package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.GridKt
import com.github.mvysny.kaributesting.v10.PrettyPrintTree
import com.github.mvysny.kaributesting.v10.RenderersKt
import com.github.mvysny.kaributools.GridUtilsKt
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.grid.FooterRow
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.HeaderRow
import com.vaadin.flow.component.grid.editor.Editor
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
 * A set of basic extension methods for {@link Grid} and {@link TreeGrid}.
 * @author mavi
 */
@CompileStatic
class GridExtensionMethods {
    /**
     * Returns the item on given row. Fails if the row index is invalid. The data provider is
     * sorted according to given <code>sortOrders</code> (empty by default) and filtered according
     * to given <code>filter</code> (null by default) first.
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
     * Returns all items in given data provider, sorted according to given <code>sortOrders</code> (empty by default) and filtered according
     * to given <code>filter</code> (null by default).
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
     * For {@link TreeGrid} this returns the x-th displayed row; skips children of collapsed nodes.
     * Uses {@link #_rowSequence(com.vaadin.flow.component.treegrid.TreeGrid)}.
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
     * For {@link TreeGrid} this walks the {@link #_rowSequence(com.vaadin.flow.component.treegrid.TreeGrid)}.
     * <p></p>
     * WARNING: Very slow operation for {@link TreeGrid}.
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
     * For {@link TreeGrid} this returns all displayed rows; skips children of collapsed nodes.
     * @return the list of items.
     */
    @NotNull
    static <T> List<T> _findAll(@NotNull Grid<T> self) {
        return GridKt._findAll(self)
    }

    /**
     * Returns the number of items in this data provider.
     * <p></p>
     * In case of {@link HierarchicalDataProvider}
     * this returns the number of ALL items including all leafs.
     */
    static <T, F> int _size(@NotNull DataProvider<T, F> self, @Nullable F filter = null) {
        return GridKt._size(self, filter)
    }

    /**
     * Returns the number of items in this data provider, including child items.
     * The function traverses recursively until all children are found; then a total size
     * is returned. The function uses {@link HierarchicalDataProvider#size(com.vaadin.flow.data.provider.Query)} mostly, but
     * also uses {@link HierarchicalDataProvider#fetchChildren(com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery)} to discover children.
     * Only children matching <code>filter</code> are considered for recursive computation of
     * the size.
     * <p></p>
     * Note that this can differ to {@link #_size(com.vaadin.flow.component.grid.Grid)} since <code>Grid._size()</code> ignores children
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
     * Gets a {@link Grid.Column} of this grid by its <code>columnKey</code>.
     * @throws AssertionError if no such column exists.
     */
    @NotNull
    static <T> Grid.Column<T> _getColumnByKey(@NotNull Grid<T> self, @NotNull String columnKey) {
        return GridKt._getColumnByKey(self, columnKey)
    }

    /**
     * Performs a click on a {@link com.vaadin.flow.data.renderer.ClickableRenderer} in given {@link Grid} cell.
     * Only supports the following scenarios:
     * <ul>
     *     <li>{@link com.vaadin.flow.data.renderer.ClickableRenderer}</li>
     *     <li>{@link com.vaadin.flow.data.renderer.ComponentRenderer} which renders a {@link com.vaadin.flow.component.button.Button}
     *    or a {@link com.vaadin.flow.component.ClickNotifier}</li>
     * </ul>
     * The <code>click</code> closure is no longer supported - please see https://github.com/mvysny/karibu-testing/issues/67 for more details.
     * @param rowIndex the row index, 0 or higher.
     * @param columnKey the column key [Grid.Column.getKey]
     * @throws AssertionError if the renderer is not {@link com.vaadin.flow.data.renderer.ClickableRenderer} nor {@link com.vaadin.flow.data.renderer.ComponentRenderer}
     */
    static <T> void _clickRenderer(@NotNull Grid<T> self, int rowIndex, @NotNull String columnKey) {
        GridKt._clickRenderer(self, rowIndex, columnKey)
    }

    /**
     * Retrieves a component produced by {@link com.vaadin.flow.data.renderer.ComponentRenderer} in given Grid cell. Fails if the
     * renderer is not a {@link com.vaadin.flow.data.renderer.ComponentRenderer}.
     * @param rowIndex the row index, 0 or higher.
     * @param columnKey the column key [Grid.Column.getKey]
     * @throws AssertionError if the renderer is not {@link com.vaadin.flow.data.renderer.ComponentRenderer}.
     */
    @NotNull
    static <T> Component _getCellComponent(@NotNull Grid<T> self, int rowIndex, @NotNull String columnKey) {
        return GridKt._getCellComponent(self, rowIndex, columnKey)
    }

    /**
     * Returns the formatted value as a String. Does not use renderer to render the value - simply calls value provider and presentation provider
     * and converts the result to string (even if the result is a {@link Component}).
     * @param rowIndex the row index, 0 or higher.
     * @param columnId the column ID.
     */
    @NotNull
    static <T> String _getFormatted(@NotNull Grid<T> self, int rowIndex, @NotNull String columnKey) {
        GridKt._getFormatted(self, rowIndex, columnKey)
    }

    /**
     * Returns the formatted value as a String. Does not use renderer to render
     * the value - simply calls value provider and presentation provider
     * and converts the result to string (even if the result is a {@link Component}).
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
        com.github.mvysny.kaributools.RenderersKt.getValueProvider(self)
    }

    /**
     * Renders the template for given <code>item</code>.
     */
    @NotNull
    static <T> String renderTemplate(@NotNull TemplateRenderer<T> self, @NotNull T item) {
        RenderersKt.renderTemplate(self, item)
    }

    @NotNull
    static String getTemplate(@NotNull Renderer<?> self) {
        com.github.mvysny.kaributools.RenderersKt.getTemplate(self)
    }

    /**
     * Sets and retrieves the column header as set by {@link Grid.Column#setHeader(java.lang.String)}.
     * The result value is undefined if a component has been set as the header.
     */
    @NotNull
    static <T> String getHeader2(@NotNull Grid.Column<T> self) {
        GridUtilsKt.getHeader2(self)
    }

    /**
     * Dumps given range of <code>rows</code> of the Grid, formatting the values using the {@link #_getFormatted(com.vaadin.flow.component.grid.Grid.Column, java.lang.Object)} function. The output example:
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
     * Retrieves the cell for given <code>key</code>.
     * @param key matched against {@link Grid.Column#getKey}.
     * @return the corresponding cell
     * @throws IllegalArgumentException if no such column exists.
     */
    @NotNull
    static HeaderRow.HeaderCell getCell(@NotNull HeaderRow self, @NotNull String key) {
        GridKt.getCell(self, key)
    }

    /**
     * Retrieves the cell for given {@link Grid.Column#getKey}.
     * @param key matched against {@link Grid.Column#getKey}.
     * @return the corresponding cell
     * @throws IllegalArgumentException if no such column exists.
     */
    @NotNull
    static FooterRow.FooterCell getCell(@NotNull FooterRow self, @NotNull String key) {
        GridKt.getCell(self, key)
    }

    @Nullable
    static Renderer<?> getRenderer(@NotNull HeaderRow.HeaderCell self) {
        GridUtilsKt.getRenderer(self)
    }

    @Nullable
    static Renderer<?> getRenderer(@NotNull FooterRow.FooterCell self) {
        GridUtilsKt.getRenderer(self)
    }

    /**
     * Returns or sets the component in grid's footer cell.
     * Returns <code>null</code> if the cell contains String, something else than a component or nothing at all.
     */
    @Nullable
    static Component getComponent(@NotNull FooterRow.FooterCell self) {
        GridUtilsKt.getComponent(self)
    }

    /**
     * Returns or sets the component in grid's header cell.
     * Returns <code>null</code> if the cell contains String, something else than a component or nothing at all.
     */
    @Nullable
    static Component getComponent(@NotNull HeaderRow.HeaderCell self) {
        GridUtilsKt.getComponent(self)
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
     * Sorts given grid. Affects {@link #_findAll}, {@link #_get} and other data-fetching functions.
     */
    static <T> void sort(@NotNull Grid<T> self, QuerySortOrder... sortOrder) {
        GridUtilsKt.sort(self, sortOrder)
    }

    /**
     * Fires the {@link com.vaadin.flow.component.grid.ItemClickEvent} event for
     * given <code>rowIndex</code> which invokes all item click listeners registered via
     * {@link Grid#addItemClickListener(com.vaadin.flow.component.ComponentEventListener)}.
     * @param button the id of the pressed mouse button
     * @param ctrlKey <code>true</code> if the control key was down when the event was fired, <code>false</code> otherwise
     * @param shiftKey <code>true</code> if the shift key was down when the event was fired, <code>false</code> otherwise
     * @param altKey <code>true</code> if the alt key was down when the event was fired, <code>false</code> otherwise
     * @param metaKey <code>true</code> if the meta key was down when the event was fired, <code>false</code> otherwise
     */
    static <T> void _clickItem(@NotNull Grid<T> self, int rowIndex, int button = 1, boolean ctrlKey = false,
            boolean shiftKey = false, boolean altKey = false, boolean metaKey = false) {
        GridKt._clickItem(self, rowIndex, button, ctrlKey, shiftKey, altKey, metaKey)
    }

    /**
     * Fires the {@link com.vaadin.flow.component.grid.ItemDoubleClickEvent} event for
     * given <code>rowIndex</code> which invokes all item click listeners registered via
     * {@link Grid#addItemDoubleClickListener(com.vaadin.flow.component.ComponentEventListener)}.
     * @param button the id of the pressed mouse button
     * @param ctrlKey <code>true</code> if the control key was down when the event was fired, <code>false</code> otherwise
     * @param shiftKey <code>true</code> if the shift key was down when the event was fired, <code>false</code> otherwise
     * @param altKey <code>true</code> if the alt key was down when the event was fired, <code>false</code> otherwise
     * @param metaKey <code>true</code> if the meta key was down when the event was fired, <code>false</code> otherwise
     */
    static <T> void _doubleClickItem(@NotNull Grid<T> self, int rowIndex, int button = 1, boolean ctrlKey = false,
                               boolean shiftKey = false, boolean altKey = false, boolean metaKey = false) {
        GridKt._doubleClickItem(self, rowIndex, button, ctrlKey, shiftKey, altKey, metaKey)
    }

    /**
     * Returns a sequence which walks over all rows the {@link TreeGrid} is actually showing.
     * The sequence will *skip* children of collapsed nodes.
     * <p></p>
     * Iterating the entire sequence is a very slow operation since it will repeatedly
     * poll {@link HierarchicalDataProvider} for list of children.
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
     * Returns the number of items the {@link TreeGrid} is actually showing. For example
     * it doesn't count in children of collapsed nodes.
     *
     * A very slow operation since it walks through all items returned by {@link #_rowSequence}.
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
        GridUtilsKt.get_internalId(self)
    }

    /**
     * Call this instead of {@link Editor#editItem(java.lang.Object)} - this function makes surethat the editor opening is
     * mocked properly, calls the editor bindings, and fires the editor-open-event.
     */
    static <T> void _editItem(@NotNull Editor<T> self, T item) {
        GridKt._editItem(self, item)
    }
}
