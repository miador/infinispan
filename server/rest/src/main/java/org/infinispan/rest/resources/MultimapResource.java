package org.infinispan.rest.resources;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.infinispan.AdvancedCache;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.rest.InvocationHelper;
import org.infinispan.rest.NettyRestResponse;
import org.infinispan.rest.cachemanager.RestCacheManager;
import org.infinispan.rest.framework.ResourceHandler;
import org.infinispan.rest.framework.RestRequest;
import org.infinispan.rest.framework.RestResponse;
import org.infinispan.rest.framework.impl.Invocations;
import org.infinispan.rest.operations.exceptions.NoKeyException;

import java.util.concurrent.CompletionStage;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.infinispan.rest.framework.Method.*;
import static org.infinispan.rest.resources.MediaTypeUtils.negotiateMediaType;

public class MultimapResource implements ResourceHandler {

    private final InvocationHelper invocationHelper;

    public MultimapResource(InvocationHelper invocationHelper) {
        this.invocationHelper = invocationHelper;
    }

    @Override
    public Invocations getInvocations() {
        return new Invocations.Builder()
                .invocation().method(GET).path("/v2/multimaps/{multimapName}/{multimapKey}").handleWith(this::getMultimapValues)
                .invocation().method(POST).path( "/v2/multimaps/{multimapName}/{multimapKey}" ).handleWith( this::putValueToMultimap )
                .invocation().method(DELETE).path("/v2/multimaps/{multimapName}/{multimapKey}").handleWith( this::deleteKeyValue )
                .create();
    }

    private CompletionStage<RestResponse> deleteKeyValue( RestRequest request ) {

        return null;
    }

    private CompletionStage<RestResponse> putValueToMultimap( RestRequest request ) {

        return null;
    }

    private CompletionStage<RestResponse> getMultimapValues( RestRequest request ) {
        String multimapName = request.variables().get("multimapName");

        MediaType keyContentType = request.keyContentType();
        AdvancedCache<?, ?> cache = invocationHelper.getRestCacheManager().getCache(multimapName, request);
        MediaType requestedMediaType = negotiateMediaType(cache, invocationHelper.getEncoderRegistry(), request);

        Object key = getKey(request);

        RestCacheManager<Object> restCacheManager = invocationHelper.getRestCacheManager();
        return restCacheManager.getInternalEntry(multimapName, key, keyContentType, requestedMediaType, request).thenApply(entry -> {
            NettyRestResponse.Builder responseBuilder = new NettyRestResponse.Builder();
            responseBuilder.status(HttpResponseStatus.NOT_FOUND);

            return responseBuilder.build();
        });
    }

    private Object getKey(RestRequest request) {
        Object keyRequest = request.variables().get("multimapKey");

        if (keyRequest == null) throw new NoKeyException();

        return keyRequest.toString().getBytes(UTF_8);
    }

}
