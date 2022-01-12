package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.ironlist.IronList
import groovy.transform.CompileStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Merely a test of the API. No need to test the implementation, it is thoroughly tested elsewhere.
 * @author mavi
 */
@CompileStatic
class IronListUtilsTest {
    @BeforeEach void setup() {
        MockVaadin.setup()
    }
    @AfterEach void teardown() { MockVaadin.tearDown() }

    @Test
    void apiTest() {
        def list = new IronList<String>()
        list.setItems(["a", "b"])
        list._get(0)
        list._fetch(0, 1)
        list._findAll()
        list._size()
        list._renderer
        list._getFormattedRow(0)
        list._dump()
        list.expectRows(2)
        list.expectRow(0, "a")
    }
}
