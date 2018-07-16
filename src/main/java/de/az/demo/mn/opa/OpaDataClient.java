package de.az.demo.mn.opa;

import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.Client;
import io.reactivex.Single;

import java.util.Map;

/**
 * OPA API Client.
 */
@Client("http://${demo.opa.host:localhost}:${demo.opa.port:8181}")
interface OpaDataClient {

    /**
     * Tells whether calling mn.demo is allowed.
     *
     * @param input input data as a map. Content is defined by the policy
     * @return OPA response containing the boolean result
     */
    @Post(value = "/v1/data/mn/demo/allow")
    Single<OpaDataResponse<Boolean>> isMnDemoAllowed(Map<String, Object> input);

}
