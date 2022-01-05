package com.spacesoldier.rservice.streaming.routing.entities.stream;

import lombok.Builder;
import lombok.Data;

import java.util.function.Function;

@Data @Builder
public class StreamNode {
    private String streamName;
    private String nodeName;
    private Class transformationInputType;
    private Class transformationOutputType;
    private Function transformation;
}
