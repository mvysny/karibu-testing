import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.littemplate.MockInstantiatorV18
import java.net.URL
import kotlin.test.expect

class AllTests : DynaTest({
    beforeEach {
        MockVaadinHelper.instantiatorFactory = { MockInstantiatorV18(it) }
    }

    test("flow-build-info.json exists") {
        val res: URL? = Thread.currentThread().contextClassLoader.getResource("META-INF/VAADIN/config/flow-build-info.json")
        expect(true, "flow-build-info.json is not on the classpath!") { res != null }
    }

    group("Vaadin env") {
        beforeEach { MockVaadin.setup() }
        afterEach { MockVaadin.tearDown() }

        test("Vaadin version") {
            expect(18) { VaadinMeta.version }
            expect(false) { VaadinMeta.isCompatibilityMode }
        }
    }
    allTests()
})
