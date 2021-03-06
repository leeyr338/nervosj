package org.nervos.appchain.protocol;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.nervos.appchain.protocol.core.Request;
import org.nervos.appchain.protocol.core.Response;

/**
 * Services API.
 */
public interface NervosjService {
    <T extends Response> T send(
            Request request, Class<T> responseType) throws IOException;

    <T extends Response> CompletableFuture<T> sendAsync(
            Request request, Class<T> responseType);
}
