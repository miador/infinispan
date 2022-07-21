package org.infinispan.multimap.configuration;

import org.infinispan.commons.configuration.Builder;
import org.infinispan.commons.configuration.attributes.Attribute;
import org.infinispan.commons.configuration.attributes.AttributeSet;

import static org.infinispan.multimap.configuration.MultimapConfiguration.SUPPORTS_DUPLICATES;

public class MultimapConfigurationBuilder implements Builder<MultimapConfiguration> {

    private final AttributeSet attributes = MultimapConfiguration.attributeDefinitionSet();

    @Override
    public void validate() {
        attributes.attributes().forEach(Attribute::validate);
    }

    @Override
    public MultimapConfiguration create() {
        return new MultimapConfiguration(attributes.protect());
    }

    @Override
    public Builder<?> read(MultimapConfiguration template) {
        this.attributes.read(template.attributes());
        return this;
    }

    public MultimapConfigurationBuilder name(String name) {
        attributes.attribute(MultimapConfiguration.NAME).set(name);
        return this;
    }

    public MultimapConfigurationBuilder supportsDuplicates(boolean supportsDuplicates) {
        attributes.attribute(SUPPORTS_DUPLICATES).set(supportsDuplicates);
        return this;
    }
}
