package com.spacesoldier.rservice.streaming.routing;

import com.spacesoldier.rservice.streaming.routing.entities.stream.StreamNode;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.GenericTypeResolver;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class AppReadyListener implements ApplicationListener<ApplicationReadyEvent> {
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ConfigurableApplicationContext context = event.getApplicationContext();
        ReactiveStreamsBuilder rxStreamBuilder = context.getBean(ReactiveStreamsBuilder.class);

        // obtain beans of the StreamNode type from the application context
        // as a result we get the map of bean names as keys and stream nodes as values
        findStreamNodesInAppContext(context, rxStreamBuilder);


        findListsOfStreamNodesInAppContext(context, rxStreamBuilder);


        rxStreamBuilder.buildStreams();
    }

    private void findStreamNodesInAppContext(
            ConfigurableApplicationContext ctx,
            ReactiveStreamsBuilder rxStreamBuilder
    ) {
        Map<String, StreamNode> streamNodes = ctx.getBeansOfType(StreamNode.class);

        for (Map.Entry<String,StreamNode> entry: streamNodes.entrySet()){
            StreamNode nodeValue = entry.getValue();
            if (nodeValue.getNodeName() == null){
                nodeValue.setNodeName(entry.getKey());
            }
            rxStreamBuilder.register(entry.getValue());
        }
    }

    private void findListsOfStreamNodesInAppContext(
            ConfigurableApplicationContext ctx,
            ReactiveStreamsBuilder rxStreamBuilder
    ) {
        // let's get all beans of list type
        Map<String, List> listBeans = ctx.getBeansOfType(List.class);

        // then check all obtained lists
        for (Map.Entry<String,List> entry: listBeans.entrySet()){
            List listToAnalyze = entry.getValue();

            List<StreamNode> streamNodes = new ArrayList<>();

            // if our list contains stream nodes - then probably it's a stream definition
            // in form of list of nodes
            if (!listToAnalyze.isEmpty()){
                ListIterator iter = listToAnalyze.listIterator();
                // let's extract all the stream nodes into a new list
                while (iter.hasNext()){
                    Object item = iter.next();
                    if (item != null){
                        if (item instanceof StreamNode){
                            streamNodes.add((StreamNode) item);
                        }
                    }
                }
            }

            // then we register all nodes from the list
            // using the bean name as a stream name
            for (StreamNode node: streamNodes){
                if (node.getStreamName() == null || node.getStreamName().isEmpty()){
                    node.setStreamName(entry.getKey());
                }
                rxStreamBuilder.register(node);
            }

        }

    }
}
