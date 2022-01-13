package com.spacesoldier.rservice.streaming.config;

import com.spacesoldier.rservice.streaming.mbus.providers.FluxProvider;
import com.spacesoldier.rservice.streaming.mbus.router.MessageRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MessageRouterConfig {
    @Autowired
    private FluxProvider fluxProvider;

    @Bean
    public MessageRouter initMessageRouter(){
        return new MessageRouter(fluxProvider);
    }
}
