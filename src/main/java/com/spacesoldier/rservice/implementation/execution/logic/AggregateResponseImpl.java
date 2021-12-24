package com.spacesoldier.rservice.implementation.execution.logic;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spacesoldier.rservice.entities.internal.queries.CommonAPIResponse;
import com.spacesoldier.rservice.streaming.manage.MonoWiresManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.function.Function;


public class AggregateResponseImpl {

    public static final String UNIT_NAME = "rs-aggregator";

    private static final Logger logger = LoggerFactory.getLogger(UNIT_NAME);

    public static Function<CommonAPIResponse,String> sendResponseBodyForSuccessResult(MonoWiresManager monoManager){

        String sysLogMsgTemplate = "[%s]: resolve response %s";

        Gson gson = new GsonBuilder().create();

        return response -> {

            String logMsg = String.format(sysLogMsgTemplate, UNIT_NAME, response.getRequestId());

            logger.info(logMsg);

            monoManager.getInput(response.getRequestId())
                            .accept(
                                    gson.toJson(response.getPayload())
                            );

            // process the response
            response.getExchange().getResponse().setStatusCode(HttpStatus.OK);
            response.getExchange()
                    .getResponse()
                    .writeWith(
                            // obtain a Mono object which will be used by framework
                            // to retrieve the result of the request
                            monoManager.getOutput(response.getRequestId())
                    );

            return logMsg;
        };
    }

}
