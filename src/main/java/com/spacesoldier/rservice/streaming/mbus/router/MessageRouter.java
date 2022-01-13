package com.spacesoldier.rservice.streaming.mbus.router;

import com.spacesoldier.rservice.streaming.mbus.providers.FluxProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MessageRouter {

    public static final String unitName = "message router";
    private static final Logger logger = LoggerFactory.getLogger("rx router");

    // this is the main routing table
    // which allow to match the object class to the stream name
    // where a subscriber consumes the objects of this type
    private Map<Class, List<String>> routingTable = new HashMap<>();

    // by default all the messages are routed among the streams of the flux bus
    private FluxProvider fluxProvider;

    public MessageRouter(FluxProvider fluxProviderInstance){
        fluxProvider = fluxProviderInstance;
    }

    public void addRouteToStreamsByClass(String streamName, Class inputType) {
        if (!routingTable.containsKey(inputType)){
            routingTable.put(inputType,new ArrayList<>());
        }

        // to prevent duplication let's check if we already map the given input type
        // into the stream with given streamName
        if (!routingTable.get(inputType).contains(streamName)){
            routingTable.get(inputType).add(streamName);
        }
    }

    private static String logMsgTemplate = "[%s]: %s";

    public void showRoutingTable(){
        logger.info(
                String.format(
                        logMsgTemplate,
                        unitName.toUpperCase(),
                        String.format(
                                "built the following routing table: \n %s",
                                routingTable
                        )
                )
        );
    }

    public Consumer routeMessages(){
        return message -> {
            Class messageObjectType = message.getClass();

            if (routingTable.containsKey(messageObjectType)){
                List<String> streamsToSend = routingTable.get(messageObjectType);
                for (String streamName: streamsToSend){
                    // put a message into the dedicated consumer
                    fluxProvider.getSink(streamName).accept(message);
                }
            }
        };
    }
}
