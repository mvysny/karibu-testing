@file:Suppress("OverridingDeprecatedMember", "DEPRECATION")

package com.github.mvysny.kaributesting.mockhttp

import java.io.Serializable
import java.util.Enumeration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

import javax.servlet.ServletContext
import javax.servlet.http.HttpSession
import javax.servlet.http.HttpSessionContext

/**
 * A standalone implementation of the [HttpSession] interface.
 */
public open class MockHttpSession(
        private val sessionId: String,
        private val servletContext: ServletContext,
        private val creationTime: Long,
        private var maxInactiveInterval: Int
) : HttpSession, Serializable {
    private val attributes = ConcurrentHashMap<String, Any>()
    private val valid = AtomicBoolean(true)

    public val isValid: Boolean get() = valid.get()

    public constructor(session: HttpSession) : this(session.id, session.servletContext, session.lastAccessedTime, session.maxInactiveInterval) {
        copyAttributes(session)
    }

    public fun destroy() {
        attributes.clear()
    }

    override fun getCreationTime(): Long {
        checkValid()
        return creationTime
    }

    override fun getId(): String = sessionId

    override fun getLastAccessedTime(): Long {
        checkValid()
        return 0
    }

    override fun getServletContext(): ServletContext = servletContext

    override fun setMaxInactiveInterval(interval: Int) {
        this.maxInactiveInterval = interval
    }

    override fun getMaxInactiveInterval(): Int = maxInactiveInterval

    @Deprecated("Deprecated in Java")
    override fun getSessionContext(): HttpSessionContext? = null

    override fun getAttribute(name: String): Any? {
        checkValid()
        return attributes[name]
    }

    @Deprecated("Deprecated in Java")
    override fun getValue(name: String): Any? = getAttribute(name)

    override fun getAttributeNames(): Enumeration<String> {
        checkValid()
        return attributes.keys()
    }

    @Deprecated("Deprecated in Java")
    override fun getValueNames(): Array<String> = attributeNames.toList().toTypedArray()

    override fun setAttribute(name: String, value: Any?) {
        checkValid()
        attributes.putOrRemove(name, value)
    }

    @Deprecated("Deprecated in Java")
    override fun putValue(name: String, value: Any?) {
        setAttribute(name, value)
    }

    override fun removeAttribute(name: String) {
        checkValid()
        attributes.remove(name)
    }

    @Deprecated("Deprecated in Java")
    override fun removeValue(name: String) {
        removeAttribute(name)
    }

    public fun copyAttributes(httpSession: HttpSession): MockHttpSession {
        httpSession.attributeNames.toList().forEach {
            attributes[it] = httpSession.getAttribute(it)
        }
        return this
    }

    override fun invalidate() {
        checkValid()
        valid.set(false)
    }

    override fun isNew(): Boolean {
        checkValid()
        return false
    }

    private fun checkValid() {
        if (!isValid) {
            throw IllegalStateException("invalidated: $this")
        }
    }

    override fun toString(): String =
        "MockHttpSession(sessionId='$sessionId', creationTime=$creationTime, maxInactiveInterval=$maxInactiveInterval, attributes=$attributes, isValid=$isValid)"

    public companion object {
        private val sessionIdGenerator = AtomicInteger()
        public fun create(ctx: ServletContext): MockHttpSession =
            MockHttpSession(
                sessionIdGenerator.incrementAndGet().toString(),
                ctx,
                System.currentTimeMillis(),
                30
            )
    }
}
