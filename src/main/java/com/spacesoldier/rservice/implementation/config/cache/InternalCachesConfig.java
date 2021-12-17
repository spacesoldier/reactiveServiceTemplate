package com.spacesoldier.rservice.implementation.config.cache;


import com.spacesoldier.rservice.caching.EntitiesCache;
import com.spacesoldier.rservice.caching.EntityCacheStorePolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class InternalCachesConfig {

    @Bean(name = "snapshotsUpdatesCache")
    public EntitiesCache prepareSnapshotsCache(){

        return EntitiesCache.builder()
                                .cacheName("snapshotsCache")
                                .storePolicy(EntityCacheStorePolicy.OVERWRITE_EXISTING)
                                //.typeToStore(TypeToStore.class)
                                .verbose(false)
                            .build();
    }


}
