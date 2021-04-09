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
        expect("1.2.3-beta2") { SemanticVersion.fromString("1.2.3-beta2").toString() }
        expect("1.2.3-beta2") { SemanticVersion.fromString("1.2.3.beta2").toString() }
    }
    test("compare to") {
        expect(true) { SemanticVersion(14, 3, 0) > SemanticVersion(14, 2, 28) }
        expect(true) { SemanticVersion(15, 0, 0) > SemanticVersion(14, 3, 0) }
        expect(true) { SemanticVersion(17, 0, 0) > SemanticVersion(16, 0, 2) }
        expect(true) { SemanticVersion(14, 3, 0) > SemanticVersion(14, 3, 0, "beta2") }
    }
    test("is at least major,minor") {
        expect(true) { SemanticVersion(14, 3, 0).isAtLeast(14, 2) }
        expect(true) { SemanticVersion(14, 3, 0).isAtLeast(14, 3) }
        expect(false) { SemanticVersion(14, 3, 0).isAtLeast(14, 4) }
        expect(true) { SemanticVersion(14, 3, 1).isAtLeast(14, 2) }
        expect(true) { SemanticVersion(14, 3, 1).isAtLeast(14, 3) }
        expect(true) { SemanticVersion(14, 3, 0, "alpha1").isAtLeast(14, 2) }
        expect(true) { SemanticVersion(14, 3, 0, "alpha1").isAtLeast(14, 3) }
    }
    test("is at least major") {
        expect(true) { SemanticVersion(14, 3, 0).isAtLeast(14) }
        expect(true) { SemanticVersion(14, 3, 0).isAtLeast(13) }
        expect(false) { SemanticVersion(14, 3, 0).isAtLeast(15) }
        expect(true) { SemanticVersion(14, 3, 1).isAtLeast(14) }
        expect(true) { SemanticVersion(14, 3, 1).isAtLeast(13) }
        expect(true) { SemanticVersion(14, 3, 0, "alpha1").isAtLeast(14) }
        expect(true) { SemanticVersion(14, 3, 0, "alpha1").isAtLeast(13) }
    }
    test("is exactly major") {
        expect(true) { SemanticVersion(14, 3, 0).isExactly(14) }
        expect(false) { SemanticVersion(14, 3, 0).isExactly(13) }
        expect(false) { SemanticVersion(14, 3, 0).isExactly(15) }
        expect(true) { SemanticVersion(14, 3, 1).isExactly(14) }
        expect(false) { SemanticVersion(14, 3, 1).isExactly(13) }
        expect(true) { SemanticVersion(14, 3, 0, "alpha1").isExactly(14) }
        expect(false) { SemanticVersion(14, 3, 0, "alpha1").isExactly(13) }
    }
    test("is exactly major,minor") {
        expect(true) { SemanticVersion(14, 3, 0).isExactly(14, 3) }
        expect(false) { SemanticVersion(14, 3, 0).isExactly(14, 2) }
        expect(false) { SemanticVersion(14, 3, 0).isExactly(15, 0) }
        expect(true) { SemanticVersion(14, 3, 1).isExactly(14, 3) }
        expect(false) { SemanticVersion(14, 3, 1).isExactly(14, 2) }
        expect(true) { SemanticVersion(14, 3, 0, "alpha1").isExactly(14, 3) }
        expect(false) { SemanticVersion(14, 3, 0, "alpha1").isExactly(14, 2) }
    }
})
