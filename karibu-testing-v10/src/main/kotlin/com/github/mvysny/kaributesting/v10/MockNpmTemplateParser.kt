package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributesting.v10.MockNpmTemplateParser.Companion.customLoaders
import com.vaadin.flow.component.polymertemplate.NpmTemplateParser
import com.vaadin.flow.component.polymertemplate.TemplateParser
import java.io.File
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.net.URL
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Mock-loads NPM Polymer Templates from:
 * * [customLoaders]
 * * a local filesystem (`frontend/`)
 * * from classpath `META-INF/resources/frontend/`
 */
class MockNpmTemplateParser : NpmTemplateParserCopy() {

    override fun getSourcesFromTemplate(tag: String, url: String): String? {
        for (customLoader: CustomNpmTemplateLoader in customLoaders) {
            val template: String? = customLoader.getSourcesFromTemplate(tag, url)
            if (template != null) {
                return template
            }
        }

        if (url.startsWith("./")) {
            // relative URLs, located in `frontend/` folder or `META-INF/resources/frontend/` resource
            val relativeUrl: String = url.substring(2)

            // try loading from the local fs
            val frontend: File = File("frontend").absoluteFile
            val templateFile = File(frontend, relativeUrl)
            if (templateFile.exists()) {
                return templateFile.readText()
            }

            // try loading from classpath
            val classpathEntry = "META-INF/resources/frontend/$relativeUrl"
            val resource: URL? = Thread.currentThread().contextClassLoader.getResource(classpathEntry)
            if (resource != null) {
                return resource.readText()
            }
        } else {
            // probably a npm module such as @appreciated/color-picker-field
            // try the `node_modules/` folder.
            val templateFile = File("node_modules", url).absoluteFile
            if (templateFile.exists()) {
                return templateFile.readText()
            }
        }

        throw RuntimeException("""Can't load template sources for <$tag> $url. Please:
 1. make sure that the `node_modules/` folder is populated, by running mvn vaadin:prepare-frontend
 2. as a workaround, introduce your own CustomNpmTemplateLoader to MockNpmTemplateParser.customLoaders which is able to load the template""")
    }

    companion object {
        fun install() {
            // okay this is ugly as hell, but there is no other way:
            // https://github.com/vaadin/flow/issues/6537
            val instanceField: Field = NpmTemplateParser::class.java.getDeclaredField("INSTANCE").apply {
                isAccessible = true
                makeNotFinal()
            }
            val current: TemplateParser = instanceField.get(null) as TemplateParser
            if (current is NpmTemplateParser) {
                instanceField.set(null, MockNpmTemplateParser())
            }
        }

        /**
         * Register custom template loaders here if the default algorithm doesn't work for your app for some reason.
         */
        val customLoaders = CopyOnWriteArrayList<CustomNpmTemplateLoader>()
    }
}

interface CustomNpmTemplateLoader {
    fun getSourcesFromTemplate(tag: String, url: String): String?
}