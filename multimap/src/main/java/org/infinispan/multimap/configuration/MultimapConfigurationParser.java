package org.infinispan.multimap.configuration;

import org.infinispan.commons.configuration.io.ConfigurationReader;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ConfigurationParser;
import org.infinispan.configuration.parsing.Namespace;
import org.infinispan.configuration.parsing.ParseUtils;
import org.infinispan.configuration.parsing.Parser;
import org.infinispan.configuration.parsing.ParserScope;
import org.kohsuke.MetaInfServices;

import static org.infinispan.multimap.configuration.MultimapConfigurationParser.NAMESPACE;

@MetaInfServices
@Namespace(root = "multimap")
@Namespace(uri = NAMESPACE + "*", root = "multimap", since = "14.0")
public class MultimapConfigurationParser implements ConfigurationParser {

    static final String NAMESPACE = Parser.NAMESPACE + "multimap:";

    @Override
    public void readElement(ConfigurationReader reader, ConfigurationBuilderHolder holder) {
        if (!holder.inScope(ParserScope.CACHE_CONTAINER)) {
            try {
                throw new Exception("");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        GlobalConfigurationBuilder builder = holder.getGlobalConfigurationBuilder();

        Element element = Element.forName(reader.getLocalName());
        switch (element) {
            case MULTIMAP: {
                parseMultimapElement(reader, builder.addModule(MultimapConfigurationBuilder.class));
                break;
            }
            default: {
                throw ParseUtils.unexpectedElement(reader);
            }
        }
    }

    @Override
    public Namespace[] getNamespaces() {
        return ParseUtils.getNamespaceAnnotations(getClass());
    }

    private void parseMultimapElement(ConfigurationReader reader, MultimapConfigurationBuilder builder) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            ParseUtils.requireNoNamespaceAttribute(reader, i);
            String value = reader.getAttributeValue(i);
            Attribute attribute = Attribute.forName(reader.getAttributeName(i));
            switch (attribute) {
                case SUPPORTS_DUPLICATES:
                    builder.supportsDuplicates(Boolean.parseBoolean(value));
                    break;
                case NAME:
                default:
                    throw ParseUtils.unexpectedAttribute(reader, i);
            }
        }
    }
}
