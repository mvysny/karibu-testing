import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.VaadinMeta
import com.github.mvysny.kaributesting.v10.allTests
import java.net.URL
import kotlin.test.expect

class AllTests : DynaTest({
    test("flow-build-info.json exists") {
        val res: URL? = Thread.currentThread().contextClassLoader.getResource("META-INF/VAADIN/config/flow-build-info.json")
        expect(true, "flow-build-info.json is not on the classpath!") { res != null }
    }

    group("Vaadin env") {
        beforeEach { MockVaadin.setup() }
        afterEach { MockVaadin.tearDown() }

        test("Vaadin version") {
            expect(true, VaadinMeta.fullVersion.toString()) { VaadinMeta.fullVersion.isExactly(14, 6) }
            expect(false) { VaadinMeta.isCompatibilityMode }
        }
    }
    allTests(isModuleTest = false)
})
