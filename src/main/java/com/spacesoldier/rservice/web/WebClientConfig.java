package com.spacesoldier.rservice.web;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class WebClientConfig {

    @Value("${external_srv.base_url}")
    private String USR_BASE_URL;

    @Bean(name="externalAPIClient")
    public WebClient initUsrCatalogWebClient(){

        ConnectionProvider provider = ConnectionProvider
                                        .builder("customConnProvider")
                                            .maxConnections(1000)
                                            .pendingAcquireTimeout(Duration.ofSeconds(60))
                                            .pendingAcquireMaxCount(-1)
                                            .maxIdleTime(Duration.ofMillis(60))
                                        .build();//.fixed("fixed", 45, 4000, Duration.ofSeconds(6));
        HttpClient httpClient = HttpClient.create(provider);

        httpClient.doOnConnected(
                connection -> {
                        //Read write timeout setting
                        connection.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(10));
                    }
        );

        httpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        httpClient.option(ChannelOption.TCP_NODELAY, true);

        LoopResources loop = LoopResources.create(
                                            "netty-custom-loop",
                                            1,
                                            8,
                                            true
                                            );
        httpClient.runOn(loop);

        // prepare the WebClient instance before using it
        return WebClient.builder()
                            .exchangeStrategies(
                                    ExchangeStrategies.builder()
                                            .codecs(
                                                configurer -> configurer
                                                                .defaultCodecs()
                                                                .maxInMemorySize(
                                                                16 * 1024 * 1024
                                                                )
                                            )
                                    .build()
                            )
                            .clientConnector(new ReactorClientHttpConnector(httpClient))
                            .baseUrl(USR_BASE_URL)
                        .build();
    }
}
