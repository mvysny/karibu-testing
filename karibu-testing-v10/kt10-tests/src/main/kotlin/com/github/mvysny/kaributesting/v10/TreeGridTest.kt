package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.vaadin.flow.data.provider.hierarchy.TreeData
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider
import kotlin.test.expect

internal fun DynaNodeGroup.treeGridTestbatch() {

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("HierarchicalDataProvider") {
        group("_size") {
            test("simple") {
                expect(20) {
                    tree((0 until 20).toList())._size()
                }
            }
        }
    }
}

private fun <T> tree(roots: List<T>, childProvider: (T) -> List<T> = { listOf() }): TreeDataProvider<T> =
        TreeDataProvider(TreeData<T>().addItems(roots, childProvider))
