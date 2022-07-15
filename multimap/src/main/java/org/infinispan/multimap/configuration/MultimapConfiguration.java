package org.infinispan.multimap.configuration;

import org.infinispan.commons.configuration.attributes.AttributeDefinition;
import org.infinispan.commons.configuration.attributes.AttributeSet;

public class MultimapConfiguration {

    static final AttributeDefinition<String> NAME = AttributeDefinition.builder(Attribute.NAME, null, String.class)
            .validator(value -> {
                if (value == null) {
                    try {
                        throw new Exception("Missing name");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
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

    final AttributeSet attributes() {
        return attributes;
    }

    public String name() {
        return attributes.attribute(NAME).get();
    }
}
