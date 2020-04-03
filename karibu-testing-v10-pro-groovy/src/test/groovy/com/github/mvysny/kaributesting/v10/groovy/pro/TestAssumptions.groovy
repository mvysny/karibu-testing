package com.github.mvysny.kaributesting.v10.groovy.pro

import com.github.mvysny.kaributesting.v10.UtilsKt
import com.github.mvysny.kaributesting.v10.VaadinMeta
import groovy.transform.CompileStatic

import static org.junit.jupiter.api.Assumptions.assumeFalse

/**
 * @author mavi
 */
@CompileStatic
class TestAssumptions {
    static void assumeTestable() {
        assumeFalse(VaadinMeta.INSTANCE.version == 14 && UtilsKt.jvmVersion >= 13,
                "Cannot test on Java 13+ and Vaadin 14+npm, see this for more details: https://github.com/mvysny/karibu-testing/issues/29")
    }
}
