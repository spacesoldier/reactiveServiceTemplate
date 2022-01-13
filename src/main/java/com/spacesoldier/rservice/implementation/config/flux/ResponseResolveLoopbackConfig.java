package com.spacesoldier.rservice.implementation.config.flux;

import com.spacesoldier.rservice.entities.internal.queries.CommonAPIResponse;
import com.spacesoldier.rservice.implementation.execution.logic.AggregateResponseImpl;
import com.spacesoldier.rservice.streaming.mbus.providers.MonoProvider;
import com.spacesoldier.rservice.streaming.mbus.transformers.OneToOneValueTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class ResponseResolveLoopbackConfig {

    @Autowired @Qualifier("MonoWiringManager")
    private MonoProvider monoManager;

    @Bean(name = "sendResponseBodyToOutputResponse")
    public OneToOneValueTransformer sendResponseBodyToOutput(){
        Map<Class, Function> valueProcessors = new HashMap<>(){
            {
                put(
                        CommonAPIResponse.class,
                        AggregateResponseImpl.sendResponseBodyForSuccessResult(monoManager)
                );
            }
        };
        return new OneToOneValueTransformer(
                "sendResponseBodyToOutput",
                valueProcessors
        );
    }
}
