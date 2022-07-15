package org.infinispan.multimap.configuration;

import org.infinispan.commons.configuration.Builder;
import org.infinispan.commons.configuration.attributes.Attribute;
import org.infinispan.commons.configuration.attributes.AttributeSet;

public class MultimapConfigurationBuilder implements Builder<MultimapConfiguration> {

    private final AttributeSet attributes = MultimapConfiguration.attributeDefinitionSet();

    @Override
    public Builder<?> read(MultimapConfiguration template) {
        this.attributes.read(template.attributes());
        return this;
    }

    @Override
    public void validate() {
        attributes.attributes().forEach(Attribute::validate);
    }

    @Override
    public MultimapConfiguration create() {
        //FIXME
        return new MultimapConfiguration();
    }

    public MultimapConfigurationBuilder supportsDuplicates(boolean supportsDuplicates){
        attributes.attribute(MultimapConfiguration.SUPPORTS_DUPLICATES).set(supportsDuplicates);
        return this;
    }
}
