package com.spacesoldier.rservice.entities.external.io;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SuccessCallResultEnvelope {
    private String requestId;
    private Object payload;
    private Object originalRequest;
}
