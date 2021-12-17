package com.spacesoldier.rservice.implementation.kafka;

import com.glowbyte.restructure.streaming.manage.FluxWiresManager;
import com.glowbyte.restructure.streaming.transformers.kafka.LogicUnitKeyValueMapper;
import com.glowbyte.restructure.streaming.transformers.kafka.LogicUnitValueMapper;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class KafkaHandlerConfig {
    @Autowired @Qualifier("parseUpdateMessage")
    private LogicUnitKeyValueMapper parseUpdateMessage;

    @Autowired @Qualifier("handleLimitsUpdates")
    private LogicUnitValueMapper handleLimitsUpdates;

    @Bean
    public Function<KStream<String,String>, KStream<Object,Object>[]> incomingPegasusUpdatesSubscribe(){
        return limitsUpdatesStream -> limitsUpdatesStream
                .flatMap(           parseUpdateMessage      )
                .flatMapValues(     handleLimitsUpdates     )
                .branch(
                        Predicates.isLogMessage,
                        Predicates.isError
                );
    }


    @Autowired @Qualifier("FluxWiringManager")
    private FluxWiresManager fluxManager;

    // log messages output
    // @Bean
    public Supplier<Flux<Message<String>>> logMessageAsyncOutput(){
        return () -> fluxManager.getStream("logMessageOutAdapter");
    }

    // error messages output
    //@Bean
    public Supplier<Flux<Message<String>>> errorMessageAsyncOutput(){
        return () -> fluxManager.getStream("errorMessageOutAdapter");
    }
}
