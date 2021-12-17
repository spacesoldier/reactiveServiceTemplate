package com.spacesoldier.rservice.entities.io;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.server.ServerWebExchange;

@Data @Builder
public class IncomingRequestEnvelope {
    private String rqId;
    private ServerWebExchange exchange;
    private String payload;
}
