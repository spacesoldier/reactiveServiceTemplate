package com.spacesoldier.rservice.streaming.routing;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

public class AppReadyListener implements ApplicationListener<ApplicationReadyEvent> {
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        event.getApplicationContext().getBean(ReactiveStreamsBuilder.class).buildStreams();
    }
}
