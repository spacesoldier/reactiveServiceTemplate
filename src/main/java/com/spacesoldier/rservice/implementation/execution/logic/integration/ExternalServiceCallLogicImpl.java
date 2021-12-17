package com.spacesoldier.rservice.implementation.execution.logic.integration;

import com.glowbyte.restructure.entities.internal.queries.RunExternalAPICallRequest;
import com.glowbyte.restructure.web.WebCallParam;
import com.glowbyte.restructure.web.WebErrorStatusHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ExternalServiceCallLogicImpl {

    public static final String unitName = "user-catalog-caller";
    private static final Logger logger = LoggerFactory.getLogger(unitName);

    public static Function<RunExternalAPICallRequest, List> prepareCallSpecImpl(WebClient client){
        return prepareCallRq -> {
            List results = new ArrayList();

            String rqPath = prepareCallRq.getCallPath();

            WebClient.RequestBodySpec rqSpec =  prepareRequestSpec(
                    client,
                    rqPath,
                    prepareCallRq.getOriginalRequest().getRqId(),
                    new ArrayList<WebCallParam>(),
                    HttpMethod.GET
            );

            results.add(
                    RunExternalAPICallRequest.builder()
                                            .requestBodySpec(rqSpec)
                                            .originalRequest(prepareCallRq.getOriginalRequest())
                                        .build()
            );

            return results;
        };
    }

    private static WebClient.RequestBodySpec prepareRequestSpec(
            WebClient serviceClient,
            String requestPath,
            String requestId,
            List<WebCallParam> params,
            HttpMethod callMethod
    ) {
        // GET route from outbound REST API
        // process the response in non-blocking manner
        WebClient.RequestBodySpec rqSpec = serviceClient.method(callMethod)
                .uri(uriBuilder -> {
                    uriBuilder.path(requestPath);
                    if (callMethod == HttpMethod.GET){
                        if (params!= null && !params.isEmpty()){
                            params.stream()
                                    .forEach(
                                            paramEntity -> uriBuilder
                                                    .queryParam(paramEntity.getName(),
                                                            paramEntity.getValue()));
                        }

                    }
                    URI result = uriBuilder.build();
                    logger.info("[WILL CALL]: "+result.toString());
                    return result;
                });
        return rqSpec;
    }

    public static Function<RunExternalAPICallRequest, List> runExternalAPICall(
            BiConsumer onSuccess,
            BiConsumer onFail
    ){

        return runRequest -> {
            List results = new ArrayList();

            WebClient.RequestBodySpec rqSpec = runRequest.getRequestBodySpec();

            rqSpec.retrieve()
                    .onStatus(
                            HttpStatus::is3xxRedirection,
                            WebErrorStatusHandlers.handle3xxError(
                                    body -> handleResult(onFail)
                                            .accept(
                                                    KeyValue.pair(
                                                            runRequest.getOriginalRequest().getRqId(),
                                                            runRequest.getOriginalRequest()
                                                    ),
                                                    body
                                            )
                            )
                    )
                    .onStatus(
                            HttpStatus::is4xxClientError,
                            WebErrorStatusHandlers.handle4xxError(
                                    body -> handleResult(onFail)
                                            .accept(
                                                    KeyValue.pair(
                                                            runRequest.getOriginalRequest().getRqId(),
                                                            runRequest.getOriginalRequest()
                                                    ),
                                                    body
                                            )
                            )
                    )
                    .onStatus(
                            HttpStatus::is5xxServerError,
                            WebErrorStatusHandlers.handle5xxError(
                                    body -> handleResult(onFail)
                                            .accept(
                                                    KeyValue.pair(
                                                            runRequest.getOriginalRequest().getRqId(),
                                                            runRequest.getOriginalRequest()
                                                    ),
                                                    body
                                            )
                            )
                    )
                    .bodyToMono(String.class)
                    .subscribe(
                            resultObject -> handleResult(onSuccess)
                                    .accept(
                                            KeyValue.pair(
                                                    runRequest.getOriginalRequest().getRqId(),
                                                    runRequest.getOriginalRequest()
                                            ),
                                            resultObject
                                    ),
                            error -> {
                                if (error != null){
                                    logger.info(String.format("[CALL ERROR]: %s",error.getMessage()));
                                }
                            }
                            // more logic available, just FYI
				            /*,
                                () -> {
                                    log.info("Req completed");
                                },
                                subscription -> {
                                    log.info("Timeout, make some noise");
                            } */
                    );

            return results;
        };

    }

    // aggregateKV looks like KeyValue.pair( correlId, aggregateObject )
    // so the handler will rece
    private static BiConsumer<KeyValue, Object> handleResult(
            BiConsumer handler
    ){
        return (aggregateKV, result) -> handler.accept(
                aggregateKV.value,
                result
        );
    }

}
