package com.github.karibu.mockhttp

import java.io.IOException
import java.io.PrintWriter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.servlet.ServletOutputStream
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

open class MockResponse(private val session: HttpSession) : HttpServletResponse {
    override fun encodeURL(url: String): String = url

    override fun encodeUrl(url: String): String = encodeURL(url)

    val headers = ConcurrentHashMap<String, Array<String>>()

    override fun addIntHeader(name: String, value: Int) {
        addHeader(name, value.toString())
    }

    val cookies = CopyOnWriteArrayList<Cookie>()

    override fun addCookie(cookie: Cookie) {
        cookies.add(cookie)
    }

    override fun encodeRedirectUrl(url: String): String = encodeRedirectURL(url)

    override fun flushBuffer() {
    }

    override fun encodeRedirectURL(url: String): String = url

    override fun sendRedirect(location: String) {
        TODO("not implemented")
    }

    var _bufferSize = 4096

    override fun setBufferSize(size: Int) {
        _bufferSize = size
    }

    var _locale: Locale = Locale.US

    override fun getLocale(): Locale = _locale

    override fun sendError(sc: Int, msg: String?) {
        throw IOException("The app requests a failure: $sc $msg")
    }

    override fun sendError(sc: Int) {
        throw IOException("The app requests a failure: $sc")
    }

    override fun setContentLengthLong(len: Long) {
    }

    var _characterEncoding: String = "ISO-8859-1"

    override fun setCharacterEncoding(charset: String) {
        _characterEncoding = charset
    }

    override fun addDateHeader(name: String, date: Long) {
        addHeader(name, date.toString())
    }

    override fun setLocale(loc: Locale) {
        _locale = loc
    }

    override fun getHeaders(name: String): Collection<String> = headers[name]?.toList() ?: listOf()

    override fun addHeader(name: String, value: String) {
        headers.compute(name) { _, v-> (v ?: arrayOf()) + value }
    }

    override fun setContentLength(len: Int) {
    }

    override fun getBufferSize(): Int = _bufferSize

    override fun resetBuffer() {
        TODO("not implemented")
    }

    override fun reset() {
        TODO("not implemented")
    }

    override fun setDateHeader(name: String, date: Long) {
        setHeader(name, date.toString())
    }

    var _status = 200

    override fun getStatus(): Int = _status

    override fun getCharacterEncoding(): String = _characterEncoding

    override fun isCommitted(): Boolean {
        TODO("not implemented")
    }

    override fun setStatus(sc: Int) {
        _status = sc
    }

    override fun setStatus(sc: Int, sm: String?) {
        _status = sc
    }

    override fun getHeader(name: String): String? = headers[name]?.get(0)

    var _contentType: String? = null

    override fun getContentType(): String? = _contentType

    override fun getWriter(): PrintWriter {
        TODO("not implemented")
    }

    override fun containsHeader(name: String): Boolean = headers.containsKey(name)

    override fun setIntHeader(name: String, value: Int) {
        setHeader(name, value.toString())
    }

    override fun getHeaderNames(): Collection<String> = headers.keys.toSet()

    override fun setHeader(name: String, value: String) {
        headers[name] = arrayOf(value)
    }

    override fun getOutputStream(): ServletOutputStream {
        TODO("not implemented")
    }

    override fun setContentType(type: String?) {
        _contentType = type
    }
}
