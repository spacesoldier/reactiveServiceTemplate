package com.spacesoldier.rservice.streaming.routing;

import com.spacesoldier.rservice.streaming.manage.FluxWiresManager;
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
