package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.treegrid.TreeGrid
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.provider.hierarchy.TreeData
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider
import com.vaadin.flow.data.renderer.BasicRenderer
import com.vaadin.flow.data.renderer.ClickableRenderer
import com.vaadin.flow.data.renderer.NativeButtonRenderer
import com.vaadin.flow.data.renderer.NumberRenderer
import com.vaadin.flow.data.renderer.TemplateRenderer
import com.vaadin.flow.function.ValueProvider
import groovy.transform.CompileStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.text.NumberFormat

/**
 * Merely a test of the API. No need to test the implementation, it is thoroughly tested elsewhere.
 * @author mavi
 */
@CompileStatic
class GridExtensionMethodsTest {
    @BeforeEach void setup() { MockVaadin.setup() }
    @AfterEach void teardown() { MockVaadin.tearDown() }

    @Test
    void apiTest() {
        def abcDataProvider = new ListDataProvider<String>(["a", "b", "c"])
        abcDataProvider._get(0)
        abcDataProvider._get(0, ["length".asc])
        abcDataProvider._findAll()
        abcDataProvider._findAll(["length".asc])
        def grid = new Grid<String>(String, false)
        grid.setItems(["a", "b"])
        grid._get(0)
        grid._fetch(0, 1)
        grid.dataCommunicator.fetch(0, 1)
        grid._findAll()
        abcDataProvider._size()

        def treeDP = new TreeDataProvider<String>(new TreeData<String>())
        treeDP._size()
        treeDP._size("a")
        grid._size()
        grid.addColumn(ValueProvider.identity() as ValueProvider).setKey("length")
        grid._getColumnByKey("length")

        grid.addColumn(new NativeButtonRenderer<String>("foo")).setKey("foo")
        grid._clickRenderer(0, "foo")
        grid._getFormatted(0, "length")
        grid._getFormattedRow(0)
        grid._getColumnByKey("foo").getPresentationValue("a")

        new NumberRenderer<Number>(ValueProvider.identity() as ValueProvider, NumberFormat.getInstance()).getValueProvider2()
        TemplateRenderer.of("a").renderTemplate("a")
        TemplateRenderer.of("a").template

        grid._getColumnByKey("foo").header2
        grid.dump()
        grid.expectRows(2)
        grid.expectRow(0, "a", "foo")

        grid.appendHeaderRow().getCell("foo")
        grid.appendFooterRow().getCell("foo")
        grid.appendHeaderRow().getCell("foo").renderer
        grid.appendFooterRow().getCell("foo").renderer
        grid.appendHeaderRow().getCell("foo").component
        grid.appendFooterRow().getCell("foo").component

        grid.sort("length".asc)
        grid._clickItem(0)

        new TreeGrid<String>()._rowSequence().toList()
        new TreeGrid<String>()._rowSequence { true }.toList()
        new TreeGrid<String>()._size()
        new TreeGrid<String>()._dataSourceToPrettyTree()
        new TreeGrid<String>()._getRootItems()
        new TreeGrid<String>()._expandAll()

        grid._getColumnByKey("foo")._internalId
    }
}
