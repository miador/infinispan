package org.infinispan.rest.resources;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.MULTIPLE_CHOICES;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.infinispan.rest.framework.Method.GET;
import static org.infinispan.rest.framework.Method.POST;
import static org.infinispan.rest.resources.ResourceUtil.asJsonResponse;
import static org.infinispan.rest.resources.ResourceUtil.asJsonResponseFuture;
import static org.infinispan.rest.resources.ResourceUtil.isPretty;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.infinispan.AdvancedCache;
import org.infinispan.commons.CacheException;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.query.Indexer;
import org.infinispan.query.Search;
import org.infinispan.query.core.stats.SearchStatistics;
import org.infinispan.query.core.stats.SearchStatisticsSnapshot;
import org.infinispan.query.impl.ComponentRegistryUtils;
import org.infinispan.query.impl.InfinispanQueryStatisticsInfo;
import org.infinispan.query.impl.massindex.MassIndexerAlreadyStartedException;
import org.infinispan.rest.InvocationHelper;
import org.infinispan.rest.NettyRestResponse;
import org.infinispan.rest.framework.ResourceHandler;
import org.infinispan.rest.framework.RestRequest;
import org.infinispan.rest.framework.RestResponse;
import org.infinispan.rest.framework.impl.Invocations;
import org.infinispan.rest.logging.Log;
import org.infinispan.search.mapper.mapping.SearchMapping;
import org.infinispan.security.Security;
import org.infinispan.util.logging.LogFactory;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @since 10.1
 */
public class SearchAdminResource implements ResourceHandler {

   private final static Log LOG = LogFactory.getLog(SearchAdminResource.class, Log.class);

   private final InvocationHelper invocationHelper;

   public SearchAdminResource(InvocationHelper invocationHelper) {
      this.invocationHelper = invocationHelper;
   }

   @Override
   public Invocations getInvocations() {
      return new Invocations.Builder()
            .invocation().methods(POST).path("/v2/caches/{cacheName}/search/indexes").deprecated().withAction("mass-index").handleWith(this::reindex)
            .invocation().methods(POST).path("/v2/caches/{cacheName}/search/indexes").withAction("reindex").handleWith(this::reindex)
            .invocation().methods(POST).path("/v2/caches/{cacheName}/search/indexes").withAction("updateSchema").handleWith(this::updateSchema)
            .invocation().methods(POST).path("/v2/caches/{cacheName}/search/indexes").withAction("clear").handleWith(this::clearIndexes)
            .invocation().methods(GET).path("/v2/caches/{cacheName}/search/indexes/stats").deprecated().handleWith(this::indexStats)
            .invocation().methods(GET).path("/v2/caches/{cacheName}/search/query/stats").deprecated().handleWith(this::queryStats)
            .invocation().methods(POST).path("/v2/caches/{cacheName}/search/query/stats").deprecated().withAction("clear").handleWith(this::clearStats)
            .invocation().methods(GET).path("/v2/caches/{cacheName}/search/stats").handleWith(this::searchStats)
            .invocation().methods(POST).path("/v2/caches/{cacheName}/search/stats").withAction("clear").handleWith(this::clearSearchStats)
            .create();
   }

   private CompletionStage<RestResponse> searchStats(RestRequest request) {
      NettyRestResponse.Builder responseBuilder = new NettyRestResponse.Builder();

      String cacheName = request.variables().get("cacheName");
      AdvancedCache<?, ?> cache = invocationHelper.getRestCacheManager().getCache(cacheName, request);
      if (cache == null) {
         responseBuilder.status(HttpResponseStatus.NOT_FOUND);
         return null;
      }
      Configuration cacheConfiguration = SecurityActions.getCacheConfiguration(cache);
      if (!cacheConfiguration.statistics().enabled()) {
         responseBuilder.status(NOT_FOUND).build();
      }

      String scopeParam = request.getParameter("scope");
      boolean pretty = isPretty(request);
      if (scopeParam != null && scopeParam.equalsIgnoreCase("cluster")) {
         CompletionStage<SearchStatisticsSnapshot> stats = Search.getClusteredSearchStatistics(cache);
         return stats.thenApply(s -> asJsonResponse(s.toJson(), pretty));
      } else {
         return Search.getSearchStatistics(cache).computeSnapshot().thenApply(s -> asJsonResponse(s.toJson(), pretty));
      }
   }

   private CompletionStage<RestResponse> clearSearchStats(RestRequest restRequest) {
      NettyRestResponse.Builder responseBuilder = new NettyRestResponse.Builder();

      String cacheName = restRequest.variables().get("cacheName");
      AdvancedCache<?, ?> cache = invocationHelper.getRestCacheManager().getCache(cacheName, restRequest);
      if (cache == null) {
         responseBuilder.status(HttpResponseStatus.NOT_FOUND);
         return null;
      }
      Configuration cacheConfiguration = cache.getCacheConfiguration();
      if (!cacheConfiguration.statistics().enabled()) {
         return completedFuture(responseBuilder.status(NOT_FOUND.code()).build());
      }

      String scopeParam = restRequest.getParameter("scope");

      //TODO: cluster clear
      if (scopeParam != null && scopeParam.equalsIgnoreCase("cluster")) {
         throw new CacheException("NotImplemented");
      } else {
         SearchStatistics searchStatistics = Search.getSearchStatistics(cache);
         Security.doAs(restRequest.getSubject(), () -> searchStatistics.getQueryStatistics().clear());
         return completedFuture(responseBuilder.build());
      }
   }

   private CompletionStage<RestResponse> reindex(RestRequest request) {
      boolean local = Boolean.parseBoolean(request.getParameter("local"));
      return runIndexer(request, s -> local ? s.runLocal() : s.run(), true);
   }

   private CompletionStage<RestResponse> clearIndexes(RestRequest request) {
      return runIndexer(request, Indexer::remove, false);
   }

   private CompletionStage<RestResponse> updateSchema(RestRequest request) {
      NettyRestResponse.Builder responseBuilder = new NettyRestResponse.Builder();

      AdvancedCache<?, ?> cache = lookupIndexedCache(request, responseBuilder);
      int status = responseBuilder.getStatus();
      if (status < OK.code() || status >= MULTIPLE_CHOICES.code()) {
         return completedFuture(responseBuilder.build());
      }

      responseBuilder.status(NO_CONTENT);
      try {
         SearchMapping searchMapping = ComponentRegistryUtils.getSearchMapping(cache);
         searchMapping.restart();
      } catch (Exception e) {
         responseBuilder.status(INTERNAL_SERVER_ERROR).entity("Error updating the index schema " + e.getCause());
      }
      return CompletableFuture.completedFuture(responseBuilder.build());
   }

   private CompletionStage<RestResponse> indexStats(RestRequest request) {
      NettyRestResponse.Builder responseBuilder = new NettyRestResponse.Builder();

      boolean pretty = isPretty(request);
      InfinispanQueryStatisticsInfo searchStats = lookupQueryStatistics(request, responseBuilder);
      if (searchStats == null) return completedFuture(responseBuilder.build());

      return searchStats.computeLegacyIndexStatistics().thenApply(json -> asJsonResponse(json, responseBuilder, pretty));
   }

   private CompletionStage<RestResponse> queryStats(RestRequest request) {
      NettyRestResponse.Builder responseBuilder = new NettyRestResponse.Builder();

      InfinispanQueryStatisticsInfo searchStats = lookupQueryStatistics(request, responseBuilder);
      if (searchStats == null) return completedFuture(responseBuilder.build());

      return asJsonResponseFuture(searchStats.getLegacyQueryStatistics(), responseBuilder, isPretty(request));
   }

   private CompletionStage<RestResponse> clearStats(RestRequest request) {
      NettyRestResponse.Builder responseBuilder = new NettyRestResponse.Builder();
      InfinispanQueryStatisticsInfo queryStatistics = lookupQueryStatistics(request, responseBuilder);

      if (queryStatistics == null) return completedFuture(responseBuilder.build());

      responseBuilder.status(NO_CONTENT);

      queryStatistics.clear();
      return completedFuture(responseBuilder.build());
   }

   private CompletionStage<RestResponse> runIndexer(RestRequest request,
                                                    Function<Indexer, CompletionStage<Void>> op,
                                                    boolean supportAsync) {
      NettyRestResponse.Builder responseBuilder = new NettyRestResponse.Builder();

      List<String> mode = request.parameters().get("mode");

      boolean asyncParams = mode != null && !mode.isEmpty() && mode.iterator().next().equalsIgnoreCase("async");
      boolean async = asyncParams && supportAsync;

      AdvancedCache<?, ?> cache = lookupIndexedCache(request, responseBuilder);
      int status = responseBuilder.getStatus();
      if (status < OK.code() || status >= MULTIPLE_CHOICES.code()) {
         return completedFuture(responseBuilder.build());
      }

      responseBuilder.status(NO_CONTENT);

      Indexer indexer = ComponentRegistryUtils.getIndexer(cache);

      if (async) {
         try {
            LOG.asyncMassIndexerStarted();
            op.apply(indexer).whenComplete((v, e) -> {
               if (e == null) {
                  LOG.asyncMassIndexerSuccess();
               } else {
                  LOG.errorExecutingMassIndexer(e.getCause());
               }
            });
         } catch (Exception e) {
            responseBuilder.status(INTERNAL_SERVER_ERROR).entity("Error executing the MassIndexer " + e.getCause());
         }
         return CompletableFuture.completedFuture(responseBuilder.build());
      }

      return op.apply(indexer).exceptionally(e -> {
         if (e instanceof MassIndexerAlreadyStartedException) {
            responseBuilder.status(BAD_REQUEST).entity("MassIndexer already started");
         } else {
            responseBuilder.status(INTERNAL_SERVER_ERROR).entity("Error executing the MassIndexer " + e.getCause());
         }
         return null;
      }).thenApply(v -> responseBuilder.build());
   }

   private AdvancedCache<?, ?> lookupIndexedCache(RestRequest request, NettyRestResponse.Builder builder) {
      String cacheName = request.variables().get("cacheName");
      AdvancedCache<?, ?> cache = invocationHelper.getRestCacheManager().getCache(cacheName, request);
      if (cache == null) {
         builder.status(HttpResponseStatus.NOT_FOUND);
         return null;
      }
      Configuration cacheConfiguration = SecurityActions.getCacheConfiguration(cache);
      if (!cacheConfiguration.indexing().enabled()) {
         builder.entity("cache is not indexed").status(BAD_REQUEST).build();
      }
      return cache;
   }

   private AdvancedCache<?, ?> lookupCacheWithStats(RestRequest request, NettyRestResponse.Builder builder) {
      AdvancedCache<?, ?> cache = lookupIndexedCache(request, builder);
      if (cache != null) {
         Configuration cacheConfiguration = SecurityActions.getCacheConfiguration(cache);
         if (!cacheConfiguration.statistics().enabled()) {
            builder.entity("statistics not enabled").status(BAD_REQUEST);
         }
      }
      return cache;
   }

   private InfinispanQueryStatisticsInfo lookupQueryStatistics(RestRequest request, NettyRestResponse.Builder builder) {
      AdvancedCache<?, ?> cache = lookupCacheWithStats(request, builder);
      if (builder.getStatus() != HttpResponseStatus.OK.code()) {
         return null;
      }
      return ComponentRegistryUtils.getQueryStatistics(cache);
   }
}
