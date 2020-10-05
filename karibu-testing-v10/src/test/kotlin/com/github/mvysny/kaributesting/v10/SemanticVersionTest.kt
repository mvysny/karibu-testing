package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaTest
import kotlin.test.expect

/**
 * @author Martin Vysny <mavi@vaadin.com>
 */
class SemanticVersionTest : DynaTest({
    test("toString") {
        expect("1.2.3") { SemanticVersion(1, 2, 3).toString() }
        expect("1.2.3-beta2") { SemanticVersion(1, 2, 3, "beta2").toString() }
    }
    test("fromString") {
        expect(SemanticVersion(1, 2, 3)) { SemanticVersion.fromString("1.2.3") }
        expect(SemanticVersion(1, 2, 3, "beta2")) { SemanticVersion.fromString("1.2.3-beta2") }
    }
    test("compare to") {
        expect(true) { SemanticVersion(14, 3, 0) > SemanticVersion(14, 2, 28) }
        expect(true) { SemanticVersion(15, 0, 0) > SemanticVersion(14, 3, 0) }
        expect(true) { SemanticVersion(17, 0, 0) > SemanticVersion(16, 0, 2) }
        expect(true) { SemanticVersion(14, 3, 0) > SemanticVersion(14, 3, 0, "beta2") }
    }
})
