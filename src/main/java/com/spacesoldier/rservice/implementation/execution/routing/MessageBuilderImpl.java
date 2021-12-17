package com.spacesoldier.rservice.implementation.execution.routing;

import com.glowbyte.restructure.entities.internal.FluxRoutedEnvelope;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public class MessageBuilderImpl {
    public static final String UNIT_NAME = "msg-builder";

    private static final String sysLogMsgTemplate = "[%s]: Received a request %s for %s";

    public static Function envelopeToRouting(String rqId, String streamName){

        String logMsgTemplate = "[REQUEST]: %s";
        return request -> {

                log.info(
                        String.format(logMsgTemplate,request.toString())
                );

                return FluxRoutedEnvelope
                            .builder()
                            .requestKey(rqId)
                            .requestObj(request)
                            .routeToAdapter(streamName)
                        .build();
        };
    }

}