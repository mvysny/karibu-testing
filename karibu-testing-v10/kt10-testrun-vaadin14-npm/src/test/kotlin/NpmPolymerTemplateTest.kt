import com.github.appreciated.app.layout.component.applayout.LeftLayouts
import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.kaributesting.v10.MockVaadin

class NpmPolymerTemplateTest : DynaTest({
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("Instantiate LeftHybrid") {
        LeftLayouts.LeftHybrid()
    }
})
