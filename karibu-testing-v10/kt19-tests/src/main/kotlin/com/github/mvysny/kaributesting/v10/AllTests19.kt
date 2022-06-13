package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.kaributools.VaadinVersion

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

    if (VaadinVersion.get.isAtLeast(23, 1)) {
        group("Dialogs 23.1+ tests") {
            dialog23_1tests()
        }
    }
}
