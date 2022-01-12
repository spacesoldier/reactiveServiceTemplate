package com.spacesoldier.rservice.streaming.config;

import com.spacesoldier.rservice.streaming.mbus.bus.FluxBus;
import com.spacesoldier.rservice.streaming.mbus.router.MessageRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MessageRouterConfig {
    @Autowired
    private FluxBus fluxBus;

    @Bean
    public MessageRouter initMessageRouter(){
        return new MessageRouter(fluxBus);
    }
}
