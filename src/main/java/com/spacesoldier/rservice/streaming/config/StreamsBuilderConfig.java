package com.spacesoldier.rservice.streaming.config;

import com.spacesoldier.rservice.streaming.mbus.builder.ReactiveStreamsBuilder;
import com.spacesoldier.rservice.streaming.mbus.bus.FluxBus;
import com.spacesoldier.rservice.streaming.mbus.router.MessageRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class StreamsBuilderConfig {

    @Autowired
    private FluxBus fluxManager;

    @Autowired
    private MessageRouter router;

    @Bean
    public ReactiveStreamsBuilder initStreamsBuilder(){
        return new ReactiveStreamsBuilder(fluxManager, router);
    }

    @Bean
    public AppReadyListener initAppReadyListener(){
        return new AppReadyListener();
    }
}
