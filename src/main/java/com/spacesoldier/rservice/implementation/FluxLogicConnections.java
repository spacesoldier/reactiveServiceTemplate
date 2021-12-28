package com.spacesoldier.rservice.implementation;


import com.spacesoldier.rservice.streaming.manage.FluxWiresManager;
import com.spacesoldier.rservice.streaming.transformers.flux.OneToManyValueTransformer;
import com.spacesoldier.rservice.streaming.transformers.flux.OneToOneValueTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FluxLogicConnections {

    @Autowired @Qualifier("FluxWiringManager")
    private FluxWiresManager fluxManager;

    @Autowired @Qualifier("saveUpdatesToCaches")
    private OneToOneValueTransformer saveUpdatesToCaches;

    @Autowired @Qualifier("routeToFluxByName")
    private OneToOneValueTransformer routeToFluxByName;

    @Autowired @Qualifier("prepareCallSpec")
    private OneToManyValueTransformer prepareCallSpec;

    @Autowired @Qualifier("runExternalAPICall")
    private OneToManyValueTransformer runExternalAPICall;

    // This is the heart of our service - its main configuration which describes connections
    // between the small parts of its logic
    // If you read this - congratulations! You're on the right way ;)
    @Bean(name="configMainLogicNode")
    public void configMainFluxChain(){

        // here we handle snapshots, increments, tickers and users updates
        // not too much - just put them into the cache
        fluxManager.getStream("updates")
                .map(   saveUpdatesToCaches    )
                .subscribe();

        // here we call an external API (in our case - Muse endpoint to report a status)
        fluxManager.getStream("outbound_rest_call")
                .flatMap(   prepareCallSpec         )
                .flatMap(   runExternalAPICall     )
                .subscribe();

        //
        fluxManager.getStream("cj_management"     )
//                .flatMap    (                      )
//                .flatMap    (                 )
                .map        (   routeToFluxByName               )
                .subscribe();


    }

}
