package com.spacesoldier.rservice.streaming.routing.building;

import lombok.Builder;
import lombok.Data;

import java.util.function.Function;

@Data @Builder
public class StreamNode {
    private String streamName;
    private String nodeName;
    private Class transformationInputType;
    private Function transformation;
}
