package com.spacesoldier.rservice.entities.internal.log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Timestamp;
import java.util.Date;

public class LogHelper {

    public static LogMessage prepareMessage(
            String rqId,
            String moduleName,
            int statusCode,
            String statusDesc,
            String event
    ){
        LogMessage result = LogMessage.builder()
                                .rqId(rqId)
                                .statusCode(statusCode)
                                .statusDesc(statusDesc)
                                .module(moduleName)
                                .event(event)
                            .build();

        Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());
        result.setTimestamp(ts.toString());
        return result;
    };

    private static Gson gson = new GsonBuilder().serializeNulls().create();

    public static String prepareJsonMessage(LogMessage msg){
        return  gson.toJson(msg, LogMessage.class);
    }
}
