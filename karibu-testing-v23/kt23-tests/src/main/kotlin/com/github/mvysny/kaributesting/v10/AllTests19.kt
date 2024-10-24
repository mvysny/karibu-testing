package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import org.junit.jupiter.api.Nested

/**
 * @param isModuleTest if true then this test run simulates a jar reusable component.
 */
@DynaTestDsl
fun DynaNodeGroup.allTests19(isModuleTest: Boolean) {
    allTests(isModuleTest)

    group("Grid Vaadin 19+") {
        grid19Testbatch()
    }

    group("security") {
        securityTests()
    }

    group("Renderers Vaadin 22+"){
        renderers22Tests()
    }

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
}
