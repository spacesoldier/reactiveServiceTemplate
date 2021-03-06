package com.spacesoldier.rservice.streaming.config;

import com.spacesoldier.rservice.streaming.entities.stream.StreamNode;
import com.spacesoldier.rservice.streaming.mbus.builder.ReactiveStreamsBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

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

        // check all beans of type List,
        // let's treat a list of StreamNode as a stream definition
        // where all the stream nodes will be connected in one chain
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
                // set the same stream name for every node
                node.setStreamName(entry.getKey());

                // we also could leave the stream node's stream name untouched,
                // but it can potentially cause conflicts with other stream definitions from the project
                // if (node.getStreamName() == null || node.getStreamName().isEmpty()){
                //    node.setStreamName(entry.getKey());
                // }

                rxStreamBuilder.register(node);
            }

        }

    }
}
