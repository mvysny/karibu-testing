/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.mvysny.kaributesting.v10;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.polymertemplate.BundleParser;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.polymertemplate.TemplateParser;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.AnnotationReader;
import com.vaadin.flow.internal.Pair;
import com.vaadin.flow.server.DependencyFilter;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.shared.ui.Dependency;
import com.vaadin.flow.shared.ui.LoadMode;

import elemental.json.JsonObject;

/**
 * Npm template parser implementation.
 * <p>
 * The implementation scans all JsModule annotations for the given template
 * class and tries to find the one that contains template definition using the
 * tag name.
 * <p>
 * The class is Singleton. Use {@link NpmTemplateParserCopy#getInstance()} to get
 * its instance.
 *
 *
 * @author Vaadin Ltd
 * @since 2.0
 *
 * @see BundleParser
 */
public class NpmTemplateParserCopy implements TemplateParser {

    private static final TemplateParser INSTANCE = new NpmTemplateParserCopy();

    private final HashMap<String, String> cache = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private JsonObject jsonStats;

    protected NpmTemplateParserCopy() {
    }

    public static TemplateParser getInstance() {
        return INSTANCE;
    }

    @Override
    public TemplateData getTemplateContent(
            Class<? extends PolymerTemplate<?>> clazz, String tag,
            VaadinService service) {

        List<Dependency> dependencies = AnnotationReader
                .getAnnotationsFor(clazz, JsModule.class).stream()
                .map(jsModule -> new Dependency(Dependency.Type.JS_MODULE,
                        jsModule.value(),
                        LoadMode.EAGER)) // load mode doesn't matter here
                .collect(Collectors.toList());

        for (DependencyFilter filter : service.getDependencyFilters()) {
            dependencies = filter.filter(new ArrayList<>(dependencies),
                    service);
        }

        Pair<Dependency, String> chosenDep = null;

        for (Dependency dependency : dependencies) {
            if (dependency.getType() != Dependency.Type.JS_MODULE) {
                continue;
            }

            String url = dependency.getUrl();
            String source = getSourcesFromTemplate(tag, url);
            if (source == null) {
                try {
                    source = getSourcesFromStats(service, url);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            if (source == null) {
                continue;
            }
            if (chosenDep == null) {
                chosenDep = new Pair<>(dependency, source);
            }
            if (dependencyHasTagName(dependency, tag)) {
                chosenDep = new Pair<>(dependency, source);
                break;
            }
        }

        if (chosenDep != null) {

            Element templateElement = BundleParser.parseTemplateElement(
                    chosenDep.getFirst().getUrl(), chosenDep.getSecond());
            if (!JsoupUtils.getDomModule(templateElement, null).isPresent()) {
                // Template needs to be wrapped in an element with id, to look
                // like a P2 template
                Element parent = new Element(tag);
                parent.attr("id", tag);
                templateElement.appendTo(parent);
            }

            return new TemplateData(chosenDep.getFirst().getUrl(),
                    templateElement);
        }

        throw new IllegalStateException(String.format("Couldn't find the "
                        + "definition of the element with tag '%s' "
                        + "in any template file declared using '@%s' annotations. "
                        + "Check the availability of the template files in your WAR "
                        + "file or provide alternative implementation of the "
                        + "method getTemplateContent() which should return an element "
                        + "representing the content of the template file", tag,
                JsModule.class.getSimpleName()));
    }

    private boolean dependencyHasTagName(Dependency dependency, String tag) {
        String url = dependency.getUrl();
        if (url.equalsIgnoreCase(tag + ".js")) {
            return true;
        }
        url = url.toLowerCase(Locale.ENGLISH);
        return url.endsWith("/" + tag + ".js");
    }

    protected String getSourcesFromTemplate(String tag, String url) {
        InputStream content = getClass().getClassLoader()
                .getResourceAsStream(url);
        if (content != null) {
            getLogger().debug(
                    "Found sources from the tag '{}' in the template '{}'", tag,
                    url);
            return FrontendUtils.streamToString(content);
        }
        return null;
    }

    private String getSourcesFromStats(VaadinService service, String url)
            throws IOException {
        try {
            lock.lock();
            if (isStatsFileReadNeeded(service)) {
                String content = FrontendUtils.getStatsContent(service);
                if (content != null) {
                    resetCache(content);
                }
            }
        } finally {
            lock.unlock();
        }
        if (!cache.containsKey(url) && jsonStats != null) {
            cache.put(url,
                    BundleParser.getSourceFromStatistics(url, jsonStats));
        }
        return cache.get(url);
    }

    /**
     * Check status to see if stats.json needs to be loaded and parsed.
     * <p>
     * Always load if jsonStats is null, never load again when we have a bundle
     * as it never changes, always load a new stats if the hash has changed and
     * we do not have a bundle.
     *
     * @param service
     *            the Vaadin service.
     * @return {@code true} if we need to re-load and parse stats.json, else
     *         {@code false}
     */
    protected boolean isStatsFileReadNeeded(VaadinService service)
            throws IOException {
        DeploymentConfiguration config = service.getDeploymentConfiguration();
        if (jsonStats == null) {
            return true;
        } else if (usesBundleFile(config)) {
            return false;
        }
        return !jsonStats.get("hash").asString()
                .equals(FrontendUtils.getStatsHash(service));
    }

    /**
     * Check if we are running in a mode without dev server and using a pre-made
     * bundle file.
     *
     * @param config
     *            deployment configuration
     * @return true if production mode or disabled dev server
     */
    private boolean usesBundleFile(DeploymentConfiguration config) {
        return config.isProductionMode() && !config.enableDevServer();
    }

    private void resetCache(String fileContents) {
        cache.clear();
        jsonStats = BundleParser.parseJsonStatistics(fileContents);
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(NpmTemplateParserCopy.class.getName());
    }

    /**
     * Utilities for JSOUP DOM manipulations.
     *
     * @author Vaadin Ltd
     *
     */
    private static class JsoupUtils {

        private JsoupUtils() {
            // Utility class
        }

        /**
         * Removes all comments from the {@code node} tree.
         *
         * @param node
         *            a Jsoup node
         */
        static void removeCommentsRecursively(Node node) {
            int i = 0;
            while (i < node.childNodeSize()) {
                Node child = node.childNode(i);
                if (child instanceof Comment) {
                    child.remove();
                } else {
                    removeCommentsRecursively(child);
                    i++;
                }
            }
        }

        /**
         * Finds {@code "dom-module"} element inside the {@code parent}.
         * <p>
         * If {@code id} is provided then {@code "dom-module"} element is searched
         * with the given {@code id} value.
         *
         * @param parent
         *            the parent element
         * @param id
         *            optional id attribute value to search {@code "dom-module"}
         *            element, may be {@code null}
         * @return
         */
        static Optional<Element> getDomModule(Element parent, String id) {
            Stream<Element> stream = parent.getElementsByTag("dom-module").stream();
            if (id != null) {
                stream = stream.filter(element -> id.equals(element.id()));
            }
            return stream.findFirst();
        }

    }

}
