package com.spacesoldier.rservice.implementation.config.flux;

import com.spacesoldier.rservice.caching.EntitiesCache;
import com.spacesoldier.rservice.entities.internal.cache.IncomingUpdate;
import com.spacesoldier.rservice.implementation.execution.logic.caching.HandleCachesUpdatesImpl;
import com.spacesoldier.rservice.streaming.transformers.flux.OneToOneValueTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class UpdatesHandleConfig {

    @Autowired @Qualifier("snapshotsUpdatesCache")
    private EntitiesCache eventsCache;


    @Bean(name="saveUpdatesToCaches")
    public OneToOneValueTransformer saveUpdatesToCaches(){
        Map<Class, Function> valueProcessors = new HashMap<>(){
            {
                put(
                        IncomingUpdate.class,
                        HandleCachesUpdatesImpl.saveIncomingUpdate(eventsCache)
                );
            }
        };

        return new OneToOneValueTransformer(
                "saveUpdatesToCache",
                valueProcessors
        );


    }
}
