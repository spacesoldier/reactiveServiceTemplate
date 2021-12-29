package com.spacesoldier.rservice.streaming.routing;

import com.spacesoldier.rservice.streaming.manage.FluxWiresManager;
import com.spacesoldier.rservice.streaming.transformers.flux.OneToOneValueTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class FluxChainsRoutingConfig {

    @Autowired @Qualifier("FluxWiringManager")
    private FluxWiresManager fluxManager;

    @Bean(name = "routeToFluxByName")
    public OneToOneValueTransformer routeToFluxByName(){
        Map<Class, Function> valueProcessors = new HashMap<>(){
            {
                put(
                        FluxRoutedEnvelope.class,
                        FluxChainsRoutingImpl.routeEnvelopeToFlux(fluxManager)
                );
            }
        };

        return new OneToOneValueTransformer(
                "interFluxRouter",
                valueProcessors
        );
    }
}
