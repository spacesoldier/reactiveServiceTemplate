package com.spacesoldier.rservice.implementation.execution.logic.caching;

import com.spacesoldier.rservice.caching.EntitiesCache;
import com.spacesoldier.rservice.entities.internal.cache.IncomingPegasusUpdate;
import com.spacesoldier.rservice.entities.internal.log.LogHelper;
import com.spacesoldier.rservice.entities.internal.log.LogMessage;

import java.util.function.Function;

public class HandleCachesUpdatesImpl {

    private static final String unitName = "handle-updates";

    public static Function<IncomingPegasusUpdate, LogMessage> saveIncomingUpdate(EntitiesCache tikersCache){

        String okStatusDescTemplate = "ticker %s saved to cache";
        String errorStatusDescTemplate = "could not save %s to cache";

        return update -> {

            String tickerName = update.getUpdateId();

            LogMessage logMsg = LogHelper.prepareMessage(
                    tickerName,
                    unitName,
                    0,
                    String.format(okStatusDescTemplate,tickerName),
                    "ticker update saved"
            );

            tikersCache.addItem().accept(tickerName, update.getPayload());

            return logMsg;
        };
    }

}
