package com.spacesoldier.rservice.streaming.wiring;

import com.spacesoldier.rservice.streaming.mbus.providers.FluxProvider;
import com.spacesoldier.rservice.streaming.mbus.providers.MonoProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class WiringManagersConfig {
    @Bean(name="FluxWiringManager")
    public FluxProvider initFluxManager(){
        return new FluxProvider();
    }

    @Bean(name="MonoWiringManager")
    public MonoProvider initMonoManager(){
        return new MonoProvider();
    }
}
