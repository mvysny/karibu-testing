package com.github.mvysny.kaributesting.v8

import com.vaadin.navigator.View
import com.vaadin.ui.UI
import kotlin.test.expect
import kotlin.test.fail

/**
 * Returns the current view (last view that was navigated to). Returns null if there is no current UI, or there is no
 * navigator, or the navigator's current view is null.
 */
val currentView: View? get() = UI.getCurrent()?.navigator?.currentView

/**
 * Expects that given [view] is the currently displayed view.
 */
fun <V: View> expectView(view: Class<V>) {
    @Suppress("UNCHECKED_CAST")
    expect(view as Class<View>) { currentView?.javaClass }
}

/**
 * Expects that given view is the currently displayed view.
 */
inline fun <reified V: View> expectView() = expectView(V::class.java)
