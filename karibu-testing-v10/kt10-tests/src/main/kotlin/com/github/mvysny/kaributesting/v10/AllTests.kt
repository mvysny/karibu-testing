package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.kaributesting.v10.pro.AbstractGridProTests
import com.github.mvysny.kaributesting.v10.pro.richTextEditorTests
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
    @Nested inner class GridProTests : AbstractGridProTests()
    @Nested inner class ConfirmDialogTests : AbstractConfirmDialogTests()
    @Nested inner class UploadTests : AbstractUploadTests()
    @Nested inner class LoginFormTests : AbstractLoginFormTests()
    @Nested inner class FormLayoutTests : AbstractFormLayoutTests()
    @Nested inner class MenuBarTests : AbstractMenuBarTests()
    @Nested inner class ShortcutsTests : AbstractShortcutsTests()
    @Nested inner class LitTemplateTests : AbstractLitTemplateTests(isModuleTest)
    @Nested inner class NpmTemplateTests : AbstractNpmPolymerTemplateTests(isModuleTest)
    @Nested inner class DialogTests : AbstractDialogTests()
    @Nested inner class CompositeTests : AbstractCompositeTests()
    @Nested inner class RadioButtonTests : AbstractRadioButtonTests()
    @Nested inner class ListBoxTests : AbstractListBoxTests()
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
