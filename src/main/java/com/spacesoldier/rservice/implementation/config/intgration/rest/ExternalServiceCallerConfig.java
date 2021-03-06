package com.spacesoldier.rservice.implementation.config.intgration.rest;

import com.spacesoldier.rservice.entities.internal.queries.PrepareExternalAPICallRequest;
import com.spacesoldier.rservice.implementation.execution.logic.integration.rest.ExternalServiceCallLogicImpl;
import com.spacesoldier.rservice.streaming.mbus.transformers.OneToManyValueTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Component
public class ExternalServiceCallerConfig {

    @Autowired @Qualifier("externalAPIClient")
    private WebClient externalAPIClient;

    @Bean(name="prepareCallSpec")
    public OneToManyValueTransformer prepareRequestSpec(){
        Map<Class, Function> valueProcessors = new HashMap<>(){
            {
                put(
                        PrepareExternalAPICallRequest.class,
                        ExternalServiceCallLogicImpl.prepareCallSpecImpl(externalAPIClient)
                );
            }
        };

        return new OneToManyValueTransformer(
                "prepareCallSpec",
                valueProcessors
        );
    }

    @Autowired @Qualifier("onFailCatalogRsHandler")
    private BiConsumer onFailConsumer;

    @Autowired @Qualifier("onSuccessCatalogRsHandler")
    private BiConsumer onSuccessConsumer;

    @Bean(name="runExternalAPICall")
    public OneToManyValueTransformer runExternalAPICall(){
        Map<Class, Function> valueProcessors = new HashMap<>(){
            {
                put(
                        PrepareExternalAPICallRequest.class,
                        ExternalServiceCallLogicImpl.runExternalAPICall(onSuccessConsumer,onFailConsumer)
                );
            }
        };

        return new OneToManyValueTransformer(
                "prepareCallSpec",
                valueProcessors
        );
    }

}
