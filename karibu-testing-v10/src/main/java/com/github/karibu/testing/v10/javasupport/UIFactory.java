package com.github.karibu.testing.v10.javasupport;

import com.vaadin.flow.component.UI;

/**
 * This SAM hides kotlin interfaces from Java/Groovy users
 */
@FunctionalInterface
public interface UIFactory {
    UI provide();
}
