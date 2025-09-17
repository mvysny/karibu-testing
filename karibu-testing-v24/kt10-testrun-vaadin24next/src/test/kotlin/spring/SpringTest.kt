// needs to be in that package otherwise Spring autoconfiguration won't autodiscover classes or something
@file:Suppress("PackageDirectoryMismatch")
package com.github.mvysny.kaributesting.v10.spring

import org.junit.jupiter.api.Disabled

// Disabled: doesn't work with Vaadin 25 for some reason
// Pull requests welcomed.
@Disabled
class SpringTest: AbstractSpringTest()
