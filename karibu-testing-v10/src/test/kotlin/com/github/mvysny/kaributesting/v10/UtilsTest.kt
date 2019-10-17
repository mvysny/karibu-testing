package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaTest
import kotlin.test.expect

/**
 * @author mavi
 */
class UtilsTest : DynaTest({
    test("parse jvm version") {
        expect(6) { "1.6.0_23".parseJvmVersion() }
        expect(7) { "1.7.0".parseJvmVersion() }
        expect(7) { "1.7.0_80".parseJvmVersion() }
        expect(8) { "1.8.0_211".parseJvmVersion() }
        expect(9) { "9.0.1".parseJvmVersion() }
        expect(11) { "11.0.4".parseJvmVersion() }
        expect(12) { "12".parseJvmVersion() }
        expect(12) { "12.0.1".parseJvmVersion() }
    }
})