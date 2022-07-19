package org.infinispan.multimap.configuration;

import org.infinispan.commons.configuration.attributes.AttributeDefinition;
import org.infinispan.commons.configuration.attributes.AttributeSet;

public class MultimapConfiguration {

    public static final AttributeDefinition<Boolean> SUPPORTS_DUPLICATES = AttributeDefinition.builder(Attribute.SUPPORTS_DUPLICATES, false, Boolean.class)
            .immutable().build();

    final AttributeSet attributes;

    MultimapConfiguration(AttributeSet attributes) {
        this.attributes = attributes;
    }

    public boolean supportsDuplicates() {
        return attributes.attribute(SUPPORTS_DUPLICATES).get();
    }

    public AttributeSet attributes() {
        return attributes;
    }
}
