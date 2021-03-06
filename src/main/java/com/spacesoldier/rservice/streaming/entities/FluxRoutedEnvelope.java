package com.spacesoldier.rservice.streaming.entities;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class FluxRoutedEnvelope {
    private String requestKey;
    private Object requestObj;
    private String routeToAdapter;
}
