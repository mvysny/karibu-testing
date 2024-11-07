package com.github.mvysny.kaributesting.v10

import org.junit.jupiter.api.Nested

/**
 * @param isModuleTest if true then this test run simulates a jar reusable component.
 */
abstract class AbstractAllTests19(val isModuleTest: Boolean) {
    @Nested inner class AllTests10 : AbstractAllTests10(isModuleTest)
    @Nested inner class GridVaadin19Tests : AbstractGrid19Tests()
    @Nested inner class SecurityTests : AbstractSecurityTests()
    @Nested inner class RenderersVaadin22Tests : AbstractRenderers22Tests()
    @Nested inner class Dialog23_1Tests : AbstractDialog23_1tests()
    @Nested inner class VirtualListTests : AbstractVirtualListTests()
    @Nested inner class MultiSelectComboBoxTests : AbstractMultiselectComboBoxTests()
    @Nested inner class SideNavTests : AbstractSideNavTests()
    @Nested inner class TabsTests : AbstractTabsTests()
    @Nested inner class LayoutTests : AbstractLayoutTests()
}
