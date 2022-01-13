package com.spacesoldier.rservice.streaming.mbus.providers;

import com.spacesoldier.rservice.streaming.mbus.channels.MonoChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

// some sort of a dynamic storage for wires
// which could be used for connecting the reactive requests with responses
public class MonoProvider {
    private Map<String, MonoChannel> requestWires = new HashMap<>();

    private final String unitName = "mono wiring manager";
    private final Logger logger = LoggerFactory.getLogger(unitName);

    public MonoChannel newWire(String wireId){
        MonoChannel stream = null;
        if (!requestWires.containsKey(wireId)){
            stream = new MonoChannel(wireId);
            requestWires.put(wireId, stream);
        }
        return stream;
    }

    private MonoChannel wireOnDemand(String wireId){
        MonoChannel result = null;
        if (!requestWires.containsKey(wireId)){
            result = newWire(wireId);
            logger.info(String.format("New request wiring: %s", wireId));
        } else {
            result = requestWires.get(wireId);
        }
        return result;
    }

    // get the object sink for publishing the items
    public Consumer getInput(String wireId){
        return wireOnDemand(wireId).getMonoInput();
    }

    // get the mono for sending it to the clients
    // and after the request is processed we dispose the mono from our scope
    public Mono getOutput(String wireId){
        return wireOnDemand(wireId)
                .getMonoToSubscribe()
                .doOnSuccess(removeWire(wireId));
    }

    private Consumer removeWire(String wireId) {

        return input -> {
            logger.info("remove");
            requestWires.remove(wireId);
        };
    }
}
