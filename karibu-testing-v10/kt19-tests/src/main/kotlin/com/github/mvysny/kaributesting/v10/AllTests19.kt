package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup

/**
 * @param isModuleTest if true then this test run simulates a jar reusable component.
 */
fun DynaNodeGroup.allTests19(isModuleTest: Boolean) {
    allTests(isModuleTest)

    group("Grid 19") {
        grid19Testbatch()
    }

    group("security") {
        securityTests()
    }
}
