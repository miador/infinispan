package org.infinispan.multimap.impl;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.factories.annotations.InfinispanModule;
import org.infinispan.lifecycle.ModuleLifecycle;
import org.infinispan.registry.InternalCacheRegistry;

import java.util.EnumSet;

/**
 * Multimap module configuration
 *
 * @since 14.0
 */
@InfinispanModule(name = "multimap", requiredModules = "core")
public class MultimapModuleLifecycle implements ModuleLifecycle {
   public static final String MULTIMAP_CACHE_NAME = "org.infinispan.MULTIMAPS";

   @Override
   public void cacheManagerStarting(GlobalComponentRegistry gcr, GlobalConfiguration globalConfiguration) {
      InternalCacheRegistry internalCacheRegistry = gcr.getComponent(InternalCacheRegistry.class);
      internalCacheRegistry.registerInternalCache(MULTIMAP_CACHE_NAME, new ConfigurationBuilder()
            .build(), EnumSet.of(InternalCacheRegistry.Flag.EXCLUSIVE));
   }

}
