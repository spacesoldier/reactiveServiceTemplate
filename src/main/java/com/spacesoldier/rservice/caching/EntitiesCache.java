package com.spacesoldier.rservice.caching;

import lombok.Builder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Builder(builderClassName = "CustomBuilder", buildMethodName = "build")
public class EntitiesCache {
    @Getter
    private String cacheName;

    private ConcurrentHashMap<String, Object> entityStorage = new ConcurrentHashMap<>();
    private Logger logger = LoggerFactory.getLogger("entity cache");

    @Getter
    private EntityCacheStorePolicy storePolicy;

    @Getter
    private Class typeToStore;

    private boolean verbose;

    // use custom builder to solve the problem with
    // Lombok's inability to initialize collections using @Default option
    public static class CustomBuilder {

            public EntitiesCache build() {
                return new EntitiesCache(cacheName, typeToStore, verbose);
            }
        }

    // also due to reason mentioned above we need to define a simple constructor
    public EntitiesCache(
            String cacheName,
            Class typeToStore,
            //EntityCacheStorePolicy policy,
            boolean verbose
    ){
        this.cacheName = cacheName;
        this.typeToStore = typeToStore;
        //this.storePolicy = policy;
        this.verbose = verbose;
    }

    private EnumMap<EntityCacheStorePolicy,BiConsumer<String,Object>> addItemAccordingToStorePolicy = new EnumMap<>(EntityCacheStorePolicy.class){
        {
            put(
                    EntityCacheStorePolicy.OVERWRITE_EXISTING,
                    (newItemKey, newItemValue) -> entityStorage.put(newItemKey,newItemValue)
            );

            put(
                    EntityCacheStorePolicy.STORE_MANY_UNDER_SAME_KEY,
                    (newItemKey, newItemValue) -> {
                        if (entityStorage.containsKey(newItemKey)){
                            if (entityStorage.get(newItemKey) instanceof List){
                                ((List) entityStorage.get(newItemKey)).add(newItemValue);
                            } else {
                                // let's store multiple items with same key as list
                                var existingValue = entityStorage.remove(newItemKey);
                                List<Object> newValueCollection = new LinkedList<>();
                                newValueCollection.add(existingValue);
                                newValueCollection.add(newItemValue);
                                entityStorage.put(newItemKey,newItemValue);
                            }
                        } else {
                            // we obtained a first item with given key
                            entityStorage.put(newItemKey,newItemValue);
                        }
                    }
            );
        }
    };

    public BiConsumer<String, Object> addItem(){
        String logMsgTemplate = "[%s CACHE]: saved %s";
        return (newItemKey, newItemValue) -> {
            if (addItemAccordingToStorePolicy.containsKey(storePolicy)){
                addItemAccordingToStorePolicy
                        .get(storePolicy)
                        .accept(newItemKey,newItemValue);
            } else {
                // default behaviour for the case
                // when the storage policy was not explicitly defined
                addItemAccordingToStorePolicy
                        .get(EntityCacheStorePolicy.OVERWRITE_EXISTING)
                        .accept(newItemKey,newItemValue);
            }

            if (verbose){
                logger.info(
                        String.format(
                                logMsgTemplate,
                                cacheName.toUpperCase(),
                                newItemKey
                        )
                );
            }
        };
    }

    public Function<String, Object> findAllItemsByKey(){
      return itemKey -> {
          Object result = null;

          if (entityStorage.containsKey(itemKey)){
              result = entityStorage.get(itemKey);
          }

          return result;
      };
    }

    public Function<String,Object> popItemsByKey(){
        String logMsgTemplate = "[%s CACHE]: extract %s";
        return itemKey -> {
            Object result = null;

            if (entityStorage.containsKey(itemKey)){
                result = entityStorage.remove(itemKey);
            }

            if (verbose){
                if (result != null){
                    logger.info(
                            String.format(
                                    logMsgTemplate,
                                    cacheName.toUpperCase(),
                                    itemKey
                            )
                    );
                }
            }

            return result;
        };
    }

    public Consumer<String> deleteItemsByKey(){
        String logMsgTemplate = "[%s CACHE]: deleted %s";
        return itemKey -> {
            entityStorage.remove(itemKey);

            if (verbose){
                logger.info(
                        String.format(
                                logMsgTemplate,
                                cacheName.toUpperCase(),
                                itemKey
                        )
                );
            }
        };
    }
}
