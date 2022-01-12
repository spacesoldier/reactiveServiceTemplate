package com.spacesoldier.rservice.streaming.mbus.manage;

import com.spacesoldier.rservice.streaming.mbus.adapters.FluxWire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

// some sort of a dynamic storage for wires
// which could be used for connecting the reactive streams
// with a number of subscribers
public class FluxWiresManager {
    private Map<String, FluxWire> requestStreams = new HashMap<>();

    private final String unitName = "flux manager";
    private final Logger logger = LoggerFactory.getLogger(unitName);

    public FluxWire newStream(String streamName){
        FluxWire stream = null;
        if (!requestStreams.containsKey(streamName)){
            stream = new FluxWire(streamName);
            requestStreams.put(streamName, stream);
        }
        return stream;
    }

    private FluxWire streamOnDemand(String streamName){
        FluxWire result = null;
        if (!requestStreams.containsKey(streamName)){
            result = newStream(streamName);
            logger.info(String.format("New stream: %s", streamName));
        } else {
            result = requestStreams.get(streamName);
        }
        return result;
    }

    // get the object sink for publishing the items
    public Consumer getSink(String streamName){
        return streamOnDemand(streamName).getStreamInput();
    }

    // get the object flux for sending it to the clients
    public Flux getStream(String streamName){
        return streamOnDemand(streamName).getStreamToSubscribe();
    }

}
