package com.spacesoldier.rservice.implementation.execution.api;

import com.spacesoldier.rservice.entities.io.IncomingRequestEnvelope;
import com.spacesoldier.rservice.streaming.mbus.providers.FluxProvider;
import com.spacesoldier.rservice.streaming.mbus.providers.MonoProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class TypicalApiCallHandlerImpl {

    @Autowired @Qualifier("MonoWiringManager")
    private MonoProvider monoManager;

    @Autowired @Qualifier("FluxWiringManager")
    private FluxProvider fluxManager;

    public Mono<String> handleRequest(ServerWebExchange exchange) {

        // let's mark every request with its very own id
        String rqId = UUID.randomUUID().toString();

        String payload = "received payload";
        //String payload = exchange.getRequest().getPath().subPath(2).value();

        //String userLogin = userLoginWithSlash.substring(1);

        monoManager.newWire(rqId);

        // wrap request id together with, well, request
        IncomingRequestEnvelope incomingRequest = IncomingRequestEnvelope.builder()
                                                    .rqId(rqId)
                                                    .exchange(exchange)
                                                    .payload(payload)
                                                .build();

        // drop the message into our reactive hell
        fluxManager.getSink("serve").accept(incomingRequest);

        return monoManager.getOutput(rqId);
    }
}
