import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.jvmVersion
import com.github.mvysny.kaributesting.v10.*
import com.github.mvysny.kaributools.VaadinVersion
import java.net.URL
import kotlin.test.expect

class AllTests : DynaTest({
    test("flow-build-info.json exists") {
        val res: URL? = Thread.currentThread().contextClassLoader.getResource("META-INF/VAADIN/config/flow-build-info.json")
        expect(true, "flow-build-info.json is not on the classpath!") { res != null }
    }

    // Vaadin 23+ only supports JDK 11+
    if (jvmVersion >= 11) {
        group("Vaadin env") {
            beforeEach { MockVaadin.setup() }
            afterEach { MockVaadin.tearDown() }

            test("Vaadin version") {
                expect(23) { VaadinVersion.get.major }
                expect(false) { VaadinMeta.isCompatibilityMode }
            }
        }
        allTests19(isModuleTest = false)
    }
})
