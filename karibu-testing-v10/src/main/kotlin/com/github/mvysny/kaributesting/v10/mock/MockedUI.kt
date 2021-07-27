package com.github.mvysny.kaributesting.v10.mock

import com.vaadin.flow.component.UI

/**
 * A simple no-op UI used by default by [com.github.mvysny.kaributesting.v10.MockVaadin.setup]. The class is open, in order to be extensible in user's library
 */
public open class MockedUI : UI()