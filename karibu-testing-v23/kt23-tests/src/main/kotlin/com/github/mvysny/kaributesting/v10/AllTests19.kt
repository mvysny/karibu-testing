package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import org.junit.jupiter.api.Nested

/**
 * @param isModuleTest if true then this test run simulates a jar reusable component.
 */
@DynaTestDsl
fun DynaNodeGroup.allTests19(isModuleTest: Boolean) {
    group("Dialogs 23.1+ tests") {
        dialog23_1tests()
    }

    group("VirtualList") {
        virtualListTests()
    }

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
}
