package com.spacesoldier.rservice.implementation.execution.routing.kstream;

import com.spacesoldier.rservice.entities.internal.log.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class KStreamToFluxForwardingImpl {
    public static String unitName = "stream-to-flux-bridge";
    private static final Logger logger = LoggerFactory.getLogger("forward-to-flux");

    public static BiFunction<Object, Object, Object> fromKstreamToFlux(Consumer adapterSink){
        String logMsgTemplate = "Request %s forwarded to internal logic chain";
        return (keyObj, valueObj) -> {

            String logStatusDesc = "Forward object to logic";
            String logMsg = null;

            int statusCode = 0;

            if (adapterSink != null){
                adapterSink.accept(valueObj);
                logMsg = String.format(logMsgTemplate, (String)keyObj);
                //logger.info(String.format("[TO FLUX CHAIN]: %s", logMsg));
            } else {
                statusCode = -100;
                logStatusDesc = String.format("[TO FLUX CHAIN]: cannot forward to flux %s", (String)keyObj);
                logMsg = "flux adapter sink is null";
                logger.info(logStatusDesc);
            }

            return LogHelper.prepareMessage(
                    (String) keyObj,
                    unitName,
                    statusCode,
                    logStatusDesc,
                    logMsg
            );
        };
    }
}
