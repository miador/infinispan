package org.infinispan.multimap.configuration;

import org.infinispan.commons.configuration.attributes.AttributeDefinition;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.configuration.cache.Configuration;

public class MultimapConfiguration extends Configuration {

    static final AttributeDefinition<Boolean> SUPPORTS_DUPLICATES = AttributeDefinition.builder(Attribute.SUPPORTS_DUPLICATES, false, Boolean.class)
            .immutable().build();

    final AttributeSet attributes;

    MultimapConfiguration(AttributeSet attributes) {
        // FIXME: create constructor
        this.attributes = attributes;
    }


    public boolean supportsDuplicates() {
        return attributes.attribute(SUPPORTS_DUPLICATES).get();
    }
}
