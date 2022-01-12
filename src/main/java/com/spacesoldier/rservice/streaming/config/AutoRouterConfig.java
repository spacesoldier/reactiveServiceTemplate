package com.spacesoldier.rservice.streaming.config;

import com.spacesoldier.rservice.streaming.mbus.builder.ReactiveStreamsBuilder;
import com.spacesoldier.rservice.streaming.mbus.manage.FluxWiresManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AutoRouterConfig {

    @Autowired
    private FluxWiresManager fluxManager;

    @Bean
    public ReactiveStreamsBuilder initStreamsBuilder(){
        return new ReactiveStreamsBuilder(fluxManager);
    }

    @Bean
    public AppReadyListener initAppReadyListener(){
        return new AppReadyListener();
    }
}
