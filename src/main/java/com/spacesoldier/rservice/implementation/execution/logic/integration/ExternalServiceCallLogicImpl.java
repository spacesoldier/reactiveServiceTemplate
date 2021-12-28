package com.spacesoldier.rservice.implementation.execution.logic.integration;

import com.spacesoldier.rservice.entities.external.io.ExternalCallRequestAggregate;
import com.spacesoldier.rservice.entities.internal.queries.PrepareExternalAPICallRequest;
import com.spacesoldier.rservice.web.WebCallParam;
import com.spacesoldier.rservice.web.WebErrorStatusHandlers;
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

    public static Function<PrepareExternalAPICallRequest, List> prepareCallSpecImpl(WebClient client){
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
                    PrepareExternalAPICallRequest.builder()
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

    public static Function<PrepareExternalAPICallRequest, List> runExternalAPICall(
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
                                                    ExternalCallRequestAggregate.builder()
                                                                .requestId(
                                                                        runRequest.getOriginalRequest().getRqId()
                                                                )
                                                                .requestAggregate(runRequest.getOriginalRequest())
                                                            .build(),
                                                    body
                                            )
                            )
                    )
                    .onStatus(
                            HttpStatus::is4xxClientError,
                            WebErrorStatusHandlers.handle4xxError(
                                    body -> handleResult(onFail)
                                            .accept(
                                                    ExternalCallRequestAggregate.builder()
                                                                .requestId(
                                                                        runRequest.getOriginalRequest().getRqId()
                                                                )
                                                                .requestAggregate(
                                                                        runRequest.getOriginalRequest()
                                                                )
                                                            .build(),
                                                    body
                                            )
                            )
                    )
                    .onStatus(
                            HttpStatus::is5xxServerError,
                            WebErrorStatusHandlers.handle5xxError(
                                    body -> handleResult(onFail)
                                            .accept(
                                                    ExternalCallRequestAggregate.builder()
                                                                .requestId(
                                                                        runRequest.getOriginalRequest().getRqId()
                                                                )
                                                                .requestAggregate(
                                                                        runRequest.getOriginalRequest()
                                                                )
                                                            .build(),
                                                    body
                                            )
                            )
                    )
                    .bodyToMono(String.class)
                    .subscribe(
                            resultObject -> handleResult(onSuccess)
                                    .accept(
                                            ExternalCallRequestAggregate.builder()
                                                        .requestId(
                                                                runRequest.getOriginalRequest().getRqId()
                                                        )
                                                        .requestAggregate(
                                                                runRequest.getOriginalRequest()
                                                        )
                                                    .build(),
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
    private static BiConsumer<ExternalCallRequestAggregate, Object> handleResult(
            BiConsumer handler
    ){
        return (aggregateObj, result) -> handler.accept(
                aggregateObj,
                result
        );
    }

}
