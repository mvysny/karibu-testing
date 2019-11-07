import com.github.appreciated.app.layout.component.applayout.LeftLayouts
import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.jvmVersion
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.polymertemplate.PolymerTemplate
import com.vaadin.flow.templatemodel.TemplateModel
import java.lang.RuntimeException

class NpmPolymerTemplateTest : DynaTest({
    if (jvmVersion < 13) {
        beforeEach { MockVaadin.setup() }
        afterEach { MockVaadin.tearDown() }

        test("Instantiate LeftHybrid") {
            LeftLayouts.LeftHybrid()
        }

        test("test loading of stuff from node_modules") {
            ColorPickerField()
        }

        test("proper error message on unloadable component") {
            expectThrows(RuntimeException::class, "Can't load template sources for <non-existent> ./non-existent.js. Please:") {
                UnloadableComponent()
            }
        }
    } else {
        // Sorry, no support for Vaadin 14+npm on Java 13 or higher: https://github.com/mvysny/karibu-testing/issues/29
    }
})

@Tag("color-picker-field")
@NpmPackage(value = "@appreciated/color-picker-field", version = "2.0.0-beta.5")
@JsModule("@appreciated/color-picker-field/src/color-picker-field.js")
class ColorPickerField : PolymerTemplate<TemplateModel>()

@Tag("non-existent")
@JsModule("./non-existent.js")
class UnloadableComponent : PolymerTemplate<TemplateModel>()
