package com.spacesoldier.rservice.implementation.api.handlers;

import com.spacesoldier.rservice.implementation.execution.api.TypicalApiCallHandlerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RestRequestsHandler {

    @Autowired
    private TypicalApiCallHandlerImpl typicalApiService;

    public Mono<String> handleTypicalRequest(ServerWebExchange request) {

        return typicalApiService.handleRequest(request);

    }
}
