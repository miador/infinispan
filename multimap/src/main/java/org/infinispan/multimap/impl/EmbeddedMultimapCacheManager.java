package org.infinispan.multimap.impl;

import java.util.Collection;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.multimap.api.embedded.MultimapCache;
import org.infinispan.multimap.api.embedded.MultimapCacheManager;
import org.infinispan.multimap.configuration.MultimapConfiguration;

import static org.infinispan.multimap.impl.MultimapModuleLifecycle.MULTIMAP_CACHE_NAME;

/**
 * Embedded implementation of {@link MultimapCacheManager}
 *
 * @author Katia Aresti, karesti@redhat.com
 * @since 9.2
 */
public class EmbeddedMultimapCacheManager<K, V> implements MultimapCacheManager<K, V> {

   private EmbeddedCacheManager cacheManager;

   public EmbeddedMultimapCacheManager(EmbeddedCacheManager embeddedMultimapCacheManager){
      this.cacheManager = embeddedMultimapCacheManager;
   }

   @Override
   public Configuration defineConfiguration(String name, Configuration configuration) {
      return cacheManager.defineConfiguration(name, configuration);
   }

   @Override
   public Configuration defineConfiguration(String name, MultimapConfiguration configuration) {
      Configuration build = new ConfigurationBuilder()
            // add encoding protostream
            .build();
      // we add the configuration of the cache in the multimap configuration cache
      cacheManager.getCache(MULTIMAP_CACHE_NAME).putIfAbsent(name, configuration);

      // we define the configuration of the multimap using the cache configuration
      return cacheManager.defineConfiguration(name, build);
   }

   @Override
   public MultimapCache<K, V> get(String name) {
      Cache<K, Collection<V>> cache = cacheManager.getCache(name, true);
      Cache<String, MultimapConfiguration> multimaps = cacheManager.getCache(MULTIMAP_CACHE_NAME);
      MultimapConfiguration multimapConfig = multimaps.get(name);
      EmbeddedMultimapCache multimapCache = new EmbeddedMultimapCache(cache, multimapConfig.supportsDuplicates());
      return multimapCache;
   }

   @Override
   public MultimapCache<K, V> get(String name, boolean supportsDuplicates) {
      Cache<K, Collection<V>> cache = cacheManager.getCache(name, true);
      EmbeddedMultimapCache multimapCache = new EmbeddedMultimapCache(cache, supportsDuplicates);
      return multimapCache;
   }
}
