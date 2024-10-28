package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.kaributesting.v10.pro.confirmDialogTestbatch
import com.github.mvysny.kaributesting.v10.pro.gridProTestbatch
import com.github.mvysny.kaributesting.v10.pro.richTextEditorTests
import npmPolymerTemplateTestBatch
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import java.util.*

open class AbstractAllTests10(val isModuleTest: Boolean) {
    @BeforeEach fun resetLocale() {
        // make sure that Validator produces messages in English
        Locale.setDefault(Locale.ENGLISH)
    }

    @Nested inner class RoutesTests : AbstractRoutesTests()
    @Nested inner class BasicUtilsTests : AbstractBasicUtilsTests()
    @Nested inner class ElementUtilsTests : AbstractElementUtilsTests()
    @Nested inner class RenderersTests : AbstractRenderersTests()
    @Nested inner class ButtonTests : AbstractButtonTests()
    @Nested inner class HasValueTests : AbstractHasValueTests()
    @Nested inner class ComboBoxTests : AbstractComboBoxTests()
    @Nested inner class GridTests : AbstractGridTests()
    @Nested inner class TreeGridTests : AbstractTreeGridTests()
    @Nested inner class LocatorAddonsTests : AbstractLocatorAddonsTests()
    @Nested inner class LocatorJTests : AbstractLocatorJTests()
    @Nested inner class LocatorTest : AbstractLocatorTest()
    @Nested inner class LocatorTest2 : AbstractLocatorTest2()
    @Nested inner class MockVaadinTests : AbstractMockVaadinTests()
    @Nested inner class BinderTests : AbstractBinderTests()
    @Nested inner class PrettyPrintTests : AbstractPrettyPrintTreeTests()
    @Nested inner class SearchSpecTests : AbstractSearchSpecTests()
    @Nested inner class NotificationTests : AbstractNotificationsTests()
    @Nested inner class ContextMenuTests : AbstractContextMenuTests()
    @Nested inner class NavigatorTests : AbstractNavigatorTests()
    @Nested inner class RouterLinkTests : AbstractRouterLinkTests()
    @Nested inner class DownloadTests : AbstractDownloadTests()
}

/**
 * @param isModuleTest if true then this test run simulates a jar reusable component.
 */
@DynaTestDsl
fun DynaNodeGroup.allTests(isModuleTest: Boolean) {
    beforeEach {
        // make sure that Validator produces messages in English
        Locale.setDefault(Locale.ENGLISH)
    }

    group("grid pro") {
        gridProTestbatch()
    }
    group("ConfirmDialog") {
        confirmDialogTestbatch()
    }
    group("upload") {
        uploadTestbatch()
    }
    group("login form test") {
        loginFormTestbatch()
    }
    group("form layout test") {
        formLayoutTest()
    }
    group("menu bar") {
        menuBarTestbatch()
    }
    group("shortcuts") {
        shortcutsTestBatch()
    }
    group("lit template") {
        litTemplateTestBatch(isModuleTest)
    }
    group("npm PolymerTemplates") {
        npmPolymerTemplateTestBatch(isModuleTest)
    }
    group("dialog") {
        dialogTests()
    }
    group("Composite") {
        compositeTests()
    }
    group("radio button") {
        radioButtonTests()
    }
    group("ListBox") {
        listBoxTestbatch()
    }
    group("CheckboxGroup") {
        checkboxGroupTests()
    }
    group("Details") {
        detailsTests()
    }
    group("Messages") {
        messageTests()
    }
    group("HasValidation") {
        hasValidationTests()
    }
    group("Tabs") {
        tabsTestbatch()
    }
    group("RichTextEditor") {
        richTextEditorTests()
    }
}
