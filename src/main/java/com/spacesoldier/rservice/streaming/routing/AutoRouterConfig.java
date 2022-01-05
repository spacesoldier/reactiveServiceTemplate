package com.spacesoldier.rservice.streaming.routing;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AutoRouterConfig {
    @Bean
    public ReactiveStreamsBuilder initStreamsBuilder(){
        return new ReactiveStreamsBuilder();
    }

    @Bean
    public AppReadyListener initAppReadyListener(){
        return new AppReadyListener();
    }
}
