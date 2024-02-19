@file:Suppress("OverridingDeprecatedMember", "DEPRECATION")

package com.github.mvysny.kaributesting.mockhttp

import java.io.Serializable
import java.util.Enumeration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import jakarta.servlet.ServletContext
import jakarta.servlet.http.HttpSession
import jakarta.servlet.http.HttpSessionContext

/**
 * A standalone implementation of the [HttpSession] interface.
 */
public open class MockHttpSession(
        private var sessionId: String,
        private val servletContext: ServletContext,
        private val creationTime: Long,
        private var maxInactiveInterval: Int
) : HttpSession, Serializable {
    private val attributes = ConcurrentHashMap<String, Any>()
    @Volatile
    private var valid = true

    public val isValid: Boolean get() = valid

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
        // according to the servlet spec we should throw an IllegalStateException if the session is invalidated.
        // However, Spring's SecurityContextLogoutHandler calls getContext() on logout after invalidating the session,
        // which goes to VaadinAwareSecurityContextHolderStrategy.getContext() which then calls getAttribute() on the session.

        // Since it apparently works in other servlet containers, we'll disable the check by default. See MockHttpEnvironment.strictSessionValidityChecks
        // for more details.
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
        // according to the servlet spec we should throw an IllegalStateException if the session is invalidated.
        // However, Spring's SecurityContextLogoutHandler invalidates the session then
        // calls HttpSessionSecurityContextRepository.saveContextInHttpSession() which then calls setAttribute() on the session.

        // Since it apparently works in other servlet containers, we'll disable the check by default. See MockHttpEnvironment.strictSessionValidityChecks
        // for more details.
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
        valid = false
    }

    override fun isNew(): Boolean {
        checkValid()
        return false
    }

    private fun checkValid() {
        if (!isValid && MockHttpEnvironment.strictSessionValidityChecks) {
            throw IllegalStateException("invalidated: $this")
        }
    }

    override fun toString(): String =
        "MockHttpSession(sessionId='$sessionId', creationTime=$creationTime, maxInactiveInterval=$maxInactiveInterval, attributes=$attributes, isValid=$isValid)"

    public companion object {
        private val sessionIdGenerator = AtomicInteger()
        public fun create(ctx: ServletContext): MockHttpSession =
            MockHttpSession(
                generateSessionId(),
                ctx,
                System.currentTimeMillis(),
                30
            )
        private fun generateSessionId(): String = sessionIdGenerator.incrementAndGet().toString()
    }

    public fun changeSessionId(): String {
        sessionId = generateSessionId()
        return sessionId
    }
}
