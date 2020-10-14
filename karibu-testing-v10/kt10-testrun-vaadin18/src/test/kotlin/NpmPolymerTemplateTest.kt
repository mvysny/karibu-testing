@file:Suppress("DEPRECATION")

import com.github.appreciated.app.layout.component.applayout.LeftLayouts
import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.polymertemplate.PolymerTemplate
import com.vaadin.flow.templatemodel.TemplateModel
import java.lang.RuntimeException

class NpmPolymerTemplateTest : DynaTest({
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
            UnloadablePTComponent()
        }
        expectThrows(RuntimeException::class, "Can't load template sources for <non-existent3> @foo/non-existent.js. Please:") {
            UnloadablePTComponent2()
        }
    }

    test("UnloadableComponent works without any mocking") {
        UnloadableComponent()
    }
})

@Tag("color-picker-field")
@NpmPackage(value = "@appreciated/color-picker-field", version = "2.0.0-beta.5")
@JsModule("@appreciated/color-picker-field/src/color-picker-field.js")
class ColorPickerField : PolymerTemplate<TemplateModel>()

@Tag("non-existent")
@JsModule("./non-existent.js")
class UnloadablePTComponent : PolymerTemplate<TemplateModel>()

@Tag("non-existent3")
@JsModule("@foo/non-existent.js")
class UnloadablePTComponent2 : PolymerTemplate<TemplateModel>()

/**
 * Still loads and works with KT (even though the JS file is missing on the FS).
 * The reason is that only PolymerTemplate-based components actually attempt to parse the js file server-side.
 */
@Tag("non-existent2")
@JsModule("./non-existent.js")
class UnloadableComponent : Component()
