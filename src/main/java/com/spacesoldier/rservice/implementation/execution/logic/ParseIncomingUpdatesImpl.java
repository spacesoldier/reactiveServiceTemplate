package com.spacesoldier.rservice.implementation.execution.logic;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spacesoldier.rservice.entities.internal.cache.IncomingPegasusUpdate;
import com.spacesoldier.rservice.entities.internal.log.LogHelper;
import com.spacesoldier.rservice.entities.internal.log.LogMessage;
import org.apache.kafka.streams.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ParseIncomingUpdatesImpl {

    public static final String unitName = "parse-inputs";
    private static final Logger logger = LoggerFactory.getLogger("validate-inputs");

    public static BiFunction<Object, Object, List<KeyValue<Object, Object>>> parseUpdate(){
        Gson gson = new GsonBuilder().create();

        String okMsgTemplate = "parsed an incoming update for %s";
        String errMsgTemplate = "Incoming update for %s error: %s";

        return (clientIdObj, securityLimitsUpdateObj) -> {
            List<KeyValue<Object, Object>> results = new ArrayList<>();

            Object validUpdate = null;
            String clientIdStr = (String) clientIdObj;

            LogMessage logMsg = LogHelper.prepareMessage(
                    clientIdStr,
                    unitName,
                    0,
                    String.format(okMsgTemplate, (String) clientIdObj),
                    "Incoming update received"
            );

            try {
                // convert the received value into a request object
                validUpdate = gson.fromJson((String) securityLimitsUpdateObj, IncomingPegasusUpdate.class);
            } catch (Exception e){
                logMsg.setStatusCode(-100);
                logMsg.setStatusDesc(String.format(errMsgTemplate,clientIdStr, e.getMessage()));
                logMsg.setEvent("parsing incoming update error");

                reportToLog(
                        clientIdStr,
                        String.format(errMsgTemplate, clientIdStr, e.getMessage())
                );
            }

            if (validUpdate != null){
                results.add(KeyValue.pair(clientIdStr, validUpdate));
            }

            results.add(KeyValue.pair(clientIdStr, logMsg));

            return results;
        };
    }

    private static void reportToLog(
            String keyAsRequestStr,
            String problemDesc
    ){
        String logMsgTemplate = "[%s]: %s %s";
        logger.info(
                String.format(
                        logMsgTemplate,
                        unitName.toUpperCase(),
                        problemDesc,
                        keyAsRequestStr
                ));
    }

}
