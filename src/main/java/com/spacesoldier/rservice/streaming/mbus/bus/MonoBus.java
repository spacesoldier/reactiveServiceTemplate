package com.spacesoldier.rservice.streaming.mbus.bus;

import com.spacesoldier.rservice.streaming.mbus.adapters.MonoWire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

// some sort of a dynamic storage for wires
// which could be used for connecting the reactive requests with responses
public class MonoBus {
    private Map<String, MonoWire> requestWires = new HashMap<>();

    private final String unitName = "mono wiring manager";
    private final Logger logger = LoggerFactory.getLogger(unitName);

    public MonoWire newWire(String wireId){
        MonoWire stream = null;
        if (!requestWires.containsKey(wireId)){
            stream = new MonoWire(wireId);
            requestWires.put(wireId, stream);
        }
        return stream;
    }

    private MonoWire wireOnDemand(String wireId){
        MonoWire result = null;
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
