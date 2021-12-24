package com.spacesoldier.rservice.entities.external.io;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ErrorCallResponse {
    private String requestId;
    private String errorMessage;
    private Object originalRequest;
}
