package org.infinispan.multimap.configuration;

import org.infinispan.commons.configuration.io.ConfigurationReader;
import org.infinispan.configuration.parsing.*;
import org.kohsuke.MetaInfServices;

import static org.infinispan.multimap.configuration.MultimapConfigurationParser.NAMESPACE;

@MetaInfServices
@Namespace(root = "multimap")
@Namespace(uri = NAMESPACE + "*", root = "multimap", since = "14.0")
public class MultimapConfigurationParser implements ConfigurationParser {

    static final String NAMESPACE = Parser.NAMESPACE + "multimap:";

    @Override
    public void readElement(ConfigurationReader reader, ConfigurationBuilderHolder holder) {

    }

    @Override
    public Namespace[] getNamespaces() {
        return ParseUtils.getNamespaceAnnotations(getClass());
    }

    private void parseMultimapAttribute(Attribute attribute, String value, MultimapConfigurationBuilder builder) {
        switch (attribute) {
            case NAME:
            case SUPPORTS_DUPLICATES:
                builder.supportsDuplicates(Boolean.parseBoolean(value));
                break;
        }
    }
}
