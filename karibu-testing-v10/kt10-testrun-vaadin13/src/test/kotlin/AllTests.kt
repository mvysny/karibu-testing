import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.kaributesting.v10.VaadinMeta
import com.github.mvysny.kaributesting.v10.allTests
import kotlin.test.expect

class AllTests : DynaTest({
    test("Vaadin version") {
        expect(13) { VaadinMeta.version }
        expect(true) { VaadinMeta.isCompatibilityMode }
        expect(null) { VaadinMeta.flowBuildInfo }
    }
    allTests()
})
