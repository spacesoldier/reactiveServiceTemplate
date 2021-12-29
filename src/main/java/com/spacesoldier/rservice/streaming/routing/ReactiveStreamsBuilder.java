package com.spacesoldier.rservice.streaming.routing;

import com.spacesoldier.rservice.streaming.manage.FluxWiresManager;
import com.spacesoldier.rservice.streaming.routing.building.StreamNode;

import java.util.*;
import java.util.function.Function;

public class ReactiveStreamsBuilder {

    private FluxWiresManager fluxManager;

    public ReactiveStreamsBuilder(){
        fluxManager = new FluxWiresManager();
    }

    // here we store the building blocks of inner structure
    List<StreamNode> streamNodes;

    // here we store the lists of nodes corresponding to the names
    // of logic chains we build
    Map<String, List<StreamNode>> streams;

    // this is the main routing table
    // which allow to match the object class to the stream name
    // where a subscriber consumes the objects of this type
    Map<Class, List<String>> routingMap;

    // register a transformation of the stream defined by its name
    public int register(Class inputType, Function transformation, String streamName, String nodeName){
      int result = 0;

      streamNodes.add(
              StreamNode.builder()
                      .streamName(streamName)
                      .nodeName(nodeName)
                      .transformationInputType(inputType)
                      .transformation(transformation)
              .build()
      );

      return result;
    };

    public int register(Class inputType, Function transformation, String streamName){
        String nodeName = inputType.getName();
        return register(
                inputType,
                transformation,
                streamName,
                nodeName
        );
    }


    private String streamNameTemplate = "%sStream";

    public int register(Class inputType, Function transformation){
        String nodeName = inputType.getName();
        return register(
                inputType,
                transformation,
                String.format(streamNameTemplate,nodeName),
                nodeName
        );
    }

    public void buildStreams(){
        // let's collect the stream names here
        Set<String> streamNames = new HashSet<>();

        for(StreamNode node: streamNodes){
            String streamName = node.getStreamName();
            Class inputType = node.getTransformationInputType();

            streamNames.add(streamName);
            addNodeToStreamDefinition(node, streamName);
            addRouteToStreamsByClass(streamName, inputType);

        }

    }

    private void addNodeToStreamDefinition(StreamNode node, String streamName) {
        // create new stream when needed
        if (!streams.containsKey(streamName)){
            streams.put(streamName,new ArrayList<>());
        }

        streams.get(streamName).add(node);
    }

    private void addRouteToStreamsByClass(String streamName, Class inputType) {
        if (!routingMap.containsKey(inputType)){
            routingMap.put(inputType,new ArrayList<>());
        }

        // to prevent duplication let's check if we already map the given input type
        // into the stream with given streamName
        if (!routingMap.get(inputType).contains(streamName)){
            routingMap.get(inputType).add(streamName);
        }
    }


}
