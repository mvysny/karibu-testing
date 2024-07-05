package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.kaributesting.v10.pro.confirmDialogTestbatch
import com.github.mvysny.kaributesting.v10.pro.gridProTestbatch
import com.github.mvysny.kaributesting.v10.pro.richTextEditorTests
import npmPolymerTemplateTestBatch
import java.util.*

/**
 * @param isModuleTest if true then this test run simulates a jar reusable component.
 */
@DynaTestDsl
fun DynaNodeGroup.allTests(isModuleTest: Boolean) {
    beforeEach {
        // make sure that Validator produces messages in English
        Locale.setDefault(Locale.ENGLISH)
    }

    group("routes test") {
        routesTestBatch()
    }
    group("basic utils") {
        basicUtilsTestbatch()
    }
    group("element utils") {
        elementUtilsTestbatch()
    }
    group("renderers") {
        renderersTests()
    }
    group("button") {
        buttonTestbatch()
    }
    group("HasValue utils") {
        hasValueTestbatch()
    }
    group("combo box") {
        comboBoxTestbatch()
    }
    group("grid") {
        gridTestbatch()
    }
    group("tree grid") {
        treeGridTestbatch()
    }
    group("iron list") {
        ironListTestbatch()
    }
    group("locator addons") {
        locatorAddonsTests()
    }
    group("locatorj") {
        locatorJTest()
    }
    group("locator") {
        group("with lifecycle hook testing") {
            locatorTest()
        }
        group("no lifecycle hook testing") {
            locatorTest2()
        }
    }
    group("mock vaadin") {
        mockVaadinTest()
    }
    group("polymer template") {
        polymerTemplateTest()
    }
    group("pretty print tree") {
        prettyPrintTreeTest()
    }
    group("search spec") {
        searchSpecTest()
    }
    group("notifications") {
        notificationsTestBattery()
    }
    group("context menu") {
        contextMenuTestbatch()
    }
    group("navigator test") {
        navigatorTest()
    }
    group("router link tests") {
        routerLinkBatch()
    }
    group("download test battery") {
        downloadTestBattery()
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
    group("binder test") {
        binderTestbatch()
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
        dialogTestbatch()
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
