package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl

/**
 * @param isModuleTest if true then this test run simulates a jar reusable component.
 */
@DynaTestDsl
fun DynaNodeGroup.allTests19(isModuleTest: Boolean) {
    allTests(isModuleTest)

    group("Grid 19") {
        grid19Testbatch()
    }

    group("security") {
        securityTests()
    }

    group("Renderers 22"){
        renderers22Tests()
    }
}
