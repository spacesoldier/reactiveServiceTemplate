package com.spacesoldier.rservice.web;

import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

public class WebErrorStatusHandlers {

    private static final String error300Label = "Error_300x";
    private static final String error400Label = "Error_400x";
    private static final String error500Label = "Error_500x";

    public static Function<ClientResponse, Mono<? extends Throwable>> handle3xxError(
            Consumer resultHandler
    ){
        return clientResponse -> {
            clientResponse
                    .bodyToMono(String.class)
                    .subscribe(     resultHandler         );
            String errMsgTemplate = "%s Redirect error: %s";
            return Mono.error(
                    new Exception(
                            String.format(
                                    errMsgTemplate,
                                    error300Label,
                                    clientResponse.headers().asHttpHeaders().getLocation()
                            )
                    )
            );
        };
    }

    public static Function<ClientResponse, Mono<? extends Throwable>> handle4xxError(
            Consumer resultHandler
    ){
        return clientResponse -> {
            clientResponse
                    .bodyToMono(String.class)
                    .subscribe(     resultHandler         );
            String errMsgTemplate = "%s Path not found: %s";
            return Mono.error(
                    new Exception(
                            String.format(
                                    errMsgTemplate,
                                    error400Label,
                                    clientResponse.headers().asHttpHeaders().getLocation()
                            )
                    )
            );
        };
    }

    public static Function<ClientResponse, Mono<? extends Throwable>> handle5xxError(
            Consumer resultHandler
    ){
        return clientResponse -> {
            clientResponse
                    .bodyToMono(String.class)
                    .subscribe(     resultHandler         );
            String errMsgTemplate = "%s Server error: %s";
            return Mono.error(
                    new Exception(
                            String.format(
                                    errMsgTemplate,
                                    error500Label,
                                    clientResponse.headers().asHttpHeaders().getLocation()
                            )
                    )
            );
        };
    }

}
