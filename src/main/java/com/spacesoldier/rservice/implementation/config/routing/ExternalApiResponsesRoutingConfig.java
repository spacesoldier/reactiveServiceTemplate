package com.spacesoldier.rservice.implementation.config.routing;

import com.glowbyte.restructure.entities.io.IncomingRequestEnvelope;
import com.glowbyte.restructure.streaming.manage.FluxWiresManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ExternalApiResponsesRoutingConfig {

    @Autowired
    @Qualifier("FluxWiringManager")
    private FluxWiresManager fluxManager;

    @Bean(name="onFailCatalogRsHandler")
    public BiConsumer<KeyValue,Object> onFailConsumer(){
        return (kv, err) -> {

            ErrorCallResponse errResponse = ErrorCallResponse.builder()
                    .errorDesc(err.toString())
                    .originalRequest((IncomingRequestEnvelope) kv.value)
                    .build();

            fluxManager.getSink("serve").accept(errResponse);

        };
    }

    @Bean(name="onSuccessCatalogRsHandler")
    public BiConsumer<KeyValue,String> onSuccessConsumer(Class responseValueType){

        Gson gson = new GsonBuilder().create();

        return (kv, userDataObj) -> {

            String userDataStr = (String) userDataObj;

            SuccessCallResultMsg resultMsg = gson.fromJson(userDataStr, responseValueType);

            SuccessCallResultEnvelope okResponse = SuccessCallResultEnvelope.builder()
                    .payload(resultMsg)
                    .originalRequest((IncomingRequestEnvelope) kv.value)
                    .build();

            fluxManager.getSink("serve").accept(okResponse);

        };
    }
}
