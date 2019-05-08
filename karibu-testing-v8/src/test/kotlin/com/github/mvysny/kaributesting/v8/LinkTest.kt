package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.annotations.Theme
import com.vaadin.navigator.Navigator
import com.vaadin.server.*
import com.vaadin.ui.Link
import com.vaadin.ui.UI
import java.io.File
import java.lang.IllegalStateException
import kotlin.test.expect

class LinkTest : DynaTest({
    beforeEach {
        MockVaadin.setup()
        UI.getCurrent().apply {
            navigator = Navigator(this, this)
            navigator.addView("myjavaview", LocatorJApiTest.MyJavaView::class.java)
        }
    }
    afterEach { MockVaadin.tearDown() }

    test("simple navigation") {
        Link("foo", ExternalResource("myjavaview"))._click()
        expectView<LocatorJApiTest.MyJavaView>()
    }

    test("navigation to external system fails") {
        expectThrows(IllegalArgumentException::class) {
            Link("foo", ExternalResource("https://www.github.com"))._click()
        }
    }

    test("clicking disabled link fails") {
        expectThrows(IllegalStateException::class, "The Link[DISABLED, caption='foo'] is not enabled") {
            Link("foo", ExternalResource("https://www.github.com")).apply {
                isEnabled = false
                _click()
            }
        }
    }

    group("download resource") {
        test("download theme") {
            expect("test!") { ThemeResource("img/test.txt").download().toString(Charsets.UTF_8) }
        }
        test("custom theme") {
            MockVaadin.tearDown()
            MockVaadin.setup({ MyThemeUI() })
            expect("mytheme-test") { ThemeResource("img/test.txt").download().toString(Charsets.UTF_8) }
        }
        test("download connector") {
            val contents = FileResource(File("build.gradle.kts")).download().toString(Charsets.UTF_8)
            expect(true, contents) { contents.contains("configureBintray") }
            expect("link!") { ClassResource(LinkTest::class.java, "link.txt").download().toString(Charsets.UTF_8) }
        }
        test("relative paths in theme resource") {
            expect("mytheme-test") { ThemeResource("../mytheme/img/test.txt").download().toString(Charsets.UTF_8) }
        }
        test("relative paths in theme resource in jar file") {
            ThemeResource("../../widgetsets/com.vaadin.DefaultWidgetSet/clear.cache.gif").download()
        }
    }

    group("download link") {
        test("disabled link") {
            expectThrows(IllegalStateException::class, "The Link[DISABLED, caption='foo'] is not enabled") {
                Link("foo", ExternalResource("https://www.github.com")).apply {
                    isEnabled = false
                    _download()
                }
            }
        }
        test("class resource") {
            val link = Link("foo", ClassResource(LinkTest::class.java, "link.txt"))
            expect("link!") { link._download().toString(Charsets.UTF_8) }
        }
    }
})

@Theme("mytheme")
class MyThemeUI : UI() {
    override fun init(request: VaadinRequest) {
    }
}