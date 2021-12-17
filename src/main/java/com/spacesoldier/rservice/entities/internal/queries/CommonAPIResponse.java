package com.spacesoldier.rservice.entities.internal.queries;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.server.ServerWebExchange;

@Data @Builder
public class CommonAPIResponse {
    private String requestId;
    private ServerWebExchange exchange;
    private Object payload;
}
