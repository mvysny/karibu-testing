package com.github.karibu.mockhttp

import java.util.*
import javax.servlet.*

open class MockServletConfig(val context: ServletContext) : ServletConfig {
    override fun getInitParameter(name: String): String? = null

    override fun getInitParameterNames(): Enumeration<String> = Collections.emptyEnumeration()

    override fun getServletName(): String = "Vaadin Servlet"

    override fun getServletContext(): ServletContext = context
}

internal fun <K, V> MutableMap<K, V>.putOrRemove(key: K, value: V?) {
    if (value == null) remove(key) else set(key, value)
}
