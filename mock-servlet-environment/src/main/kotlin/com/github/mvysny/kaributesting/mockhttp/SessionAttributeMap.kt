package com.github.mvysny.kaributesting.mockhttp

import java.util.AbstractMap
import javax.servlet.http.HttpSession

/**
 * A live map of all attributes in this session. Modifications to the map will be
 * reflected to the session and vice versa.
 */
public val HttpSession.attributes: MutableMap<String, Any>
    get() = SessionAttributeMap(this)

private class SessionAttributeEntrySetIterator(val session: HttpSession) : MutableIterator<MutableMap.MutableEntry<String, Any>> {
    /**
     * Copy the attribute names, otherwise [remove] would throw [ConcurrentModificationException].
     */
    private val attrNames: Iterator<String> = session.attributeNames.toList().iterator()
    private var lastAttributeName: String? = null

    override fun hasNext(): Boolean = attrNames.hasNext()

    override fun next(): MutableMap.MutableEntry<String, Any> {
        if (!hasNext()) {
            throw NoSuchElementException()
        }
        lastAttributeName = attrNames.next()
        return AbstractMap.SimpleEntry<String, Any>(lastAttributeName!!, session.getAttribute(lastAttributeName!!))
    }

    override fun remove() {
        check(lastAttributeName != null)
        session.removeAttribute(lastAttributeName!!)
        lastAttributeName = null
    }
}

private class SessionAttributeEntrySet(val session: HttpSession) : AbstractMutableSet<MutableMap.MutableEntry<String, Any>>() {
    override val size: Int
        get() = session.attributeNames.asSequence().count()

    override fun isEmpty(): Boolean = !session.attributeNames.hasMoreElements()

    override fun add(element: MutableMap.MutableEntry<String, Any>): Boolean {
        val modified: Boolean = session.getAttribute(element.key) != element.value
        if (modified) {
            session.setAttribute(element.key, element.value)
        }
        return modified
    }

    override fun iterator(): MutableIterator<MutableMap.MutableEntry<String, Any>> =
            SessionAttributeEntrySetIterator(session)

    override fun remove(element: MutableMap.MutableEntry<String, Any>): Boolean {
        val contains: Boolean = contains(element)
        if (contains) {
            session.removeAttribute(element.key)
        }
        return contains
    }

    override fun contains(element: MutableMap.MutableEntry<String, Any>): Boolean =
            session.getAttribute(element.key) == element.value
}

private class SessionAttributeMap(val session: HttpSession) : AbstractMutableMap<String, Any>() {
    override val entries: MutableSet<MutableMap.MutableEntry<String, Any>> =
            SessionAttributeEntrySet(session)

    override fun get(key: String): Any? = session.getAttribute(key)

    override fun put(key: String, value: Any): Any? {
        val old: Any? = session.getAttribute(key)
        session.setAttribute(key, value)
        return old
    }

    override fun remove(key: String): Any? {
        val old: Any? = session.getAttribute(key)
        session.removeAttribute(key)
        return old
    }

    override fun remove(key: String, value: Any): Boolean = if (get(key) == value) {
        remove(key)
        true
    } else {
        false
    }

    override fun isEmpty(): Boolean = entries.isEmpty()

    override fun containsKey(key: String): Boolean = session.getAttribute(key) != null
}
