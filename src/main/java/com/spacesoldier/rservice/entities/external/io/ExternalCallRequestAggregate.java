package com.spacesoldier.rservice.entities.external.io;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ExternalCallRequestAggregate {
    private String requestId;
    private Object requestAggregate;
}
