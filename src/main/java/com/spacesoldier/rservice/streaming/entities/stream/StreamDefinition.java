package com.spacesoldier.rservice.streaming.entities.stream;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
public class StreamDefinition {
    private String streamName;
    private StreamStatus state;
    private List<StreamNode> streamNodes;
}
