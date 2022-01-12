package com.spacesoldier.rservice.implementation.config.intgration.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spacesoldier.rservice.entities.external.io.ErrorCallResponse;
import com.spacesoldier.rservice.entities.external.io.ExternalCallRequestAggregate;
import com.spacesoldier.rservice.entities.external.io.SuccessCallResultEnvelope;
import com.spacesoldier.rservice.entities.io.IncomingRequestEnvelope;
import com.spacesoldier.rservice.streaming.mbus.bus.FluxBus;
import org.apache.kafka.streams.KeyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Component
public class ExternalApiResponsesRoutingConfig {

    @Autowired
    @Qualifier("FluxWiringManager")
    private FluxBus fluxManager;

    @Bean(name="onFailCatalogRsHandler")
    public BiConsumer<KeyValue,Object> onFailConsumer(){
        return (kv, err) -> {

            ErrorCallResponse errResponse = ErrorCallResponse.builder()
                    .requestId(((IncomingRequestEnvelope) kv.value).getRqId())
                    .errorMessage(err.toString())
                    .originalRequest((IncomingRequestEnvelope) kv.value)
                    .build();

            fluxManager.getSink("serve").accept(errResponse);

        };
    }

    @Bean(name="onSuccessCatalogRsHandler")
    public BiConsumer<ExternalCallRequestAggregate,String> onSuccessConsumer(){

        Gson gson = new GsonBuilder().create();

        return (aggr, userDataObj) -> {

            String userDataStr = (String) userDataObj;

            SuccessCallResultEnvelope okResponse = SuccessCallResultEnvelope.builder()
                        .requestId(
                                ((IncomingRequestEnvelope) aggr.getRequestAggregate()).getRqId()
                        )
                        .payload(userDataObj)
                        .originalRequest(aggr.getRequestAggregate())
                    .build();

            fluxManager.getSink("serve").accept(okResponse);

        };
    }
}
