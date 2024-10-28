package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import org.junit.jupiter.api.Nested

/**
 * @param isModuleTest if true then this test run simulates a jar reusable component.
 */
@DynaTestDsl
fun DynaNodeGroup.allTests19(isModuleTest: Boolean) {
    group("MultiselectComboBox") {
        multiselectComboBoxTests()
    }

    group("SideNav") {
        sideNavTests()
    }

    group("Tabs+TabSheet") {
        tabsTests()
    }
}

abstract class AbstractAllTests19(val isModuleTest: Boolean) {
    @Nested inner class AllTests10 : AbstractAllTests10(isModuleTest)
    @Nested inner class GridVaadin19Tests : AbstractGrid19Tests()
    @Nested inner class SecurityTests : AbstractSecurityTests()
    @Nested inner class RenderersVaadin22Tests : AbstractRenderers22Tests()
    @Nested inner class Dialog23_1Tests : AbstractDialog23_1tests()
    @Nested inner class VirtualListTests : AbstractVirtualListTests()
}
