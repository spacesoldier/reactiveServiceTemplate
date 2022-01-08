package com.spacesoldier.rservice.streaming.routing;

import com.spacesoldier.rservice.streaming.routing.entities.stream.StreamNode;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppReadyListener implements ApplicationListener<ApplicationReadyEvent> {
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ReactiveStreamsBuilder rxStreamBuilder = event.getApplicationContext()
                                                        .getBean(ReactiveStreamsBuilder.class);

        // obtain beans of the StreamNode type from the application context
        // as a result we get the map of bean names as keys and stream nodes as values
        Map<String,StreamNode> streamNodes = event.getApplicationContext().getBeansOfType(StreamNode.class);

        event.getApplicationContext().getBeansOfType(List.class);

        for (Map.Entry<String,StreamNode> streamNode: streamNodes.entrySet()){
            rxStreamBuilder.register(streamNode.getValue());
        }

        rxStreamBuilder.buildStreams();
    }
}
