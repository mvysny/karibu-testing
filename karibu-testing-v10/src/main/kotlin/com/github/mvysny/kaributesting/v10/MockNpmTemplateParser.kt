package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.polymertemplate.BundleParser
import com.vaadin.flow.component.polymertemplate.NpmTemplateParser
import com.vaadin.flow.component.polymertemplate.PolymerTemplate
import com.vaadin.flow.component.polymertemplate.TemplateParser
import com.vaadin.flow.internal.AnnotationReader
import com.vaadin.flow.internal.Pair
import com.vaadin.flow.server.DependencyFilter
import com.vaadin.flow.server.VaadinService
import com.vaadin.flow.server.startup.FakeBrowser
import com.vaadin.flow.shared.ui.Dependency
import org.jsoup.nodes.Element
import java.io.File
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Mock-loads NPM Polymer Templates from:
 * * [customLoaders]
 * * a local filesystem (`frontend/`)
 * * from classpath `META-INF/resources/frontend/`
 */
class MockNpmTemplateParser : TemplateParser {

    override fun getTemplateContent(clazz: Class<out PolymerTemplate<*>>, tag: String, service: VaadinService): TemplateParser.TemplateData {
        // this is a complete rip-off of NpmTemplateParser.getTemplateContent() but modified
        // since we can't extend NpmTemplateParser (the constructor is private)
        // and we can't override getSourcesFromTemplate() since it's private

        val browser = FakeBrowser.getEs6()

        var dependencies: List<Dependency> = AnnotationReader.getAnnotationsFor(clazz, JsModule::class.java)
                .map { htmlImport ->
                    Dependency(Dependency.Type.JS_MODULE,
                            htmlImport.value, htmlImport.loadMode)
                }


        val filterContext = DependencyFilter.FilterContext(
                service, browser)
        for (filter in service.dependencyFilters) {
            dependencies = filter.filter(ArrayList(dependencies),
                    filterContext)
        }

        var chosenDep: Pair<Dependency, String>? = null

        for (dependency in dependencies) {
            if (dependency.type != Dependency.Type.JS_MODULE) {
                continue
            }

            val url: String = dependency.url
            val source: String = getSourcesFromTemplate(tag, url) ?: continue

            if (chosenDep == null) {
                chosenDep = Pair(dependency, source)
            }
            if (dependencyHasTagName(dependency, tag)) {
                chosenDep = Pair(dependency, source)
                break
            }
        }

        if (chosenDep != null) {
            // Template needs to be wrapped in an element with id, to look
            // like a P2 template
            val parent = Element(tag)
            parent.attr("id", tag)

            val templateElement = BundleParser.parseTemplateElement(
                    chosenDep.first.url, chosenDep.second)
            templateElement.appendTo(parent)

            return TemplateParser.TemplateData(chosenDep.first.url,
                    templateElement)
        }

        throw IllegalStateException("Failed to load a template contents for $tag class $clazz. Please register a custom template loader into MockNpmTemplateParser.customLoaders list.")
    }

    private fun getSourcesFromTemplate(tag: String, url: String): String? {
        for (customLoader in customLoaders) {
            val template = customLoader.getSourcesFromTemplate(tag, url)
            if (template != null) {
                return template
            }
        }

        // try loading from the local fs
        if (url.startsWith("./")) {
            val frontend: File = File("frontend").absoluteFile
            val templateFile = File(frontend, url.substring(2))
            if (templateFile.exists()) {
                return templateFile.readText()
            }
        }

        // try loading from classpath
        if (url.startsWith("./")) {
            val classpathEntry = "META-INF/resources/frontend/${url.substring(2)}"
            val resource = Thread.currentThread().contextClassLoader.getResource(classpathEntry)
            if (resource != null) {
                return resource.readText()
            }
        }

        return null
    }

    private fun dependencyHasTagName(dependency: Dependency, tag: String): Boolean {
        var url = dependency.url
        if (url.equals("$tag.js", ignoreCase = true)) {
            return true
        }
        url = url.toLowerCase(Locale.ENGLISH)
        return url.endsWith("/$tag.js")
    }

    companion object {
        fun install() {
            // okay this is ugly as hell, but there is no other way:
            // https://github.com/vaadin/flow/issues/6537
            val instanceField = NpmTemplateParser::class.java.getDeclaredField("INSTANCE").apply {
                isAccessible = true
                makeNotFinal()
            }
            val current = instanceField.get(null) as TemplateParser
            if (current is NpmTemplateParser) {
                instanceField.set(null, MockNpmTemplateParser())
            }
        }

        val customLoaders = CopyOnWriteArrayList<CustomNpmTemplateLoader>()
    }
}

interface CustomNpmTemplateLoader {
    fun getSourcesFromTemplate(tag: String, url: String): String?
}