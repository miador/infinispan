package org.infinispan.multimap.configuration;

import org.infinispan.commons.configuration.attributes.AttributeDefinition;
import org.infinispan.commons.configuration.attributes.AttributeSet;

public class MultimapConfiguration {

    public static final AttributeDefinition<Boolean> SUPPORTS_DUPLICATES = AttributeDefinition.builder(Attribute.SUPPORTS_DUPLICATES, false, Boolean.class)
            .immutable().build();
    static final AttributeDefinition<String> NAME = AttributeDefinition.builder(Attribute.NAME, null, String.class)
            .validator(value -> {
                if (value == null) {
                    throw new NullPointerException();
                }
            })
            .immutable()
            .build();

    final AttributeSet attributes;

    MultimapConfiguration(AttributeSet attributes) {
        this.attributes = attributes;
    }

    static AttributeSet attributeDefinitionSet() {
        return new AttributeSet(MultimapConfiguration.class, NAME);
    }

    public boolean supportsDuplicates() {
        return attributes.attribute(SUPPORTS_DUPLICATES).get();
    }

    public AttributeSet attributes() {
        return attributes;
    }

    public String name() {
        return attributes.attribute(NAME).get();
    }
}
