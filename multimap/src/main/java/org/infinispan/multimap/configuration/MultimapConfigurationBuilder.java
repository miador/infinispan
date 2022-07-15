package org.infinispan.multimap.configuration;

import org.infinispan.commons.configuration.Builder;
import org.infinispan.configuration.cache.Configuration;

public class MultimapConfigurationBuilder implements Builder<Configuration> {

    @Override
    public Configuration create() {
        return null;
    }

    @Override
    public Builder<?> read(Configuration template) {
        return null;
    }

    @Override
    public void validate() {
        Builder.super.validate();
    }
}
