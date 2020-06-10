package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.ironlist.IronList
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.LocalDateRenderer
import com.vaadin.flow.data.renderer.NativeButtonRenderer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.test.expect

internal fun DynaNodeGroup.ironListTestbatch() {

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("_dump") {
        val dp = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
        val il = IronList<TestPerson>()
        il.dataProvider = dp
        il.setRenderer { it.name }
        expect("----------------------\n--and 7 more\n") { il._dump(0 until 0) }
        expect("----------------------\n0: name 0\n1: name 1\n2: name 2\n3: name 3\n4: name 4\n--and 2 more\n") { il._dump(0 until 5) }
        expect("----------------------\n0: name 0\n1: name 1\n2: name 2\n3: name 3\n4: name 4\n5: name 5\n6: name 6\n") {
            il._dump(0 until 20)
        }
    }

    group("expectRow()") {
        test("simple") {
            val dp = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
            val il = IronList<TestPerson>()
            il.dataProvider = dp
            il.expectRows(7)
            il.expectRow(0, "TestPerson(name=name 0, age=0)")
        }

        test("failed expectRow contains ironlist dump") {
            val dp = ListDataProvider<TestPerson>((0 until 1).map { TestPerson("name $it", it) })
            val il = IronList<TestPerson>()
            il.dataProvider = dp
            expectThrows(AssertionError::class, "----------------------\n0: TestPerson(name=name 0, age=0)") {
                il.expectRow(0, "name 1")
            }
        }

        test("row out-of-bounds contains table dump") {
            val dp = ListDataProvider<TestPerson>((0 until 1).map { TestPerson("name $it", it) })
            val il = IronList<TestPerson>()
            il.dataProvider = dp
            expectThrows(AssertionError::class, "Requested to get row 3 but the data provider only has 1 rows\n----------------------\n0: TestPerson(name=name 0, age=0)") {
                il.expectRow(3, "should fail") // should fail
            }
        }
    }

    group("renderers") {
        test("valueprovider") {
            val dp = ListDataProvider<TestPerson>((0 until 1).map { TestPerson("name $it", it) })
            val il = IronList<TestPerson>()
            il.dataProvider = dp
            il.setRenderer { it.toString() }
            il.expectRow(0, "TestPerson(name=name 0, age=0)")
        }
        test("NativeButtonRenderer") {
            val dp = ListDataProvider<TestPerson>((0 until 1).map { TestPerson("name $it", it) })
            val il = IronList<TestPerson>()
            il.dataProvider = dp
            il.setRenderer(NativeButtonRenderer<TestPerson>("View", { }))
            il.expectRow(0, "View")
        }
        test("ComponentRenderer") {
            val dp = ListDataProvider<TestPerson>((0 until 1).map { TestPerson("name $it", it) })
            val il = IronList<TestPerson>()
            il.dataProvider = dp
            il.setRenderer(ComponentRenderer<Button, TestPerson> { it -> Button(it.name) })
            il.expectRow(0, "Button[caption='name 0']")
        }
        test("LocalDateRenderer") {
            val dp = ListDataProvider<TestPerson>((0 until 1).map { TestPerson("name $it", it) })
            val il = IronList<TestPerson>()
            il.dataProvider = dp
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                    .withLocale(Locale("fi", "FI"))
            il.setRenderer(LocalDateRenderer<TestPerson>({ LocalDate.of(2019, 3, 1) }, formatter))
            il.expectRow(0, "1.3.2019")
        }
    }

    group("_getFormatted()") {
        test("non-existing row") {
            val il = IronList<TestPerson>()
            expectThrows(AssertionError::class, "Requested to get row 0 but the data provider only has 0 rows") {
                il._getFormattedRow(0)
            }
        }

        test("basic") {
            val il = IronList<TestPerson>()
            il.setItems((0..10).map { TestPerson("name $it", it) })
            il.setRenderer { it.name }
            expect("name 0") {il._getFormattedRow(0) }
        }
    }
}
