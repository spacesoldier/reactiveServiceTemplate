package com.spacesoldier.rservice.streaming.wiring;

import com.spacesoldier.rservice.streaming.mbus.bus.FluxBus;
import com.spacesoldier.rservice.streaming.mbus.bus.MonoBus;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class WiringManagersConfig {
    @Bean(name="FluxWiringManager")
    public FluxBus initFluxManager(){
        return new FluxBus();
    }

    @Bean(name="MonoWiringManager")
    public MonoBus initMonoManager(){
        return new MonoBus();
    }
}
