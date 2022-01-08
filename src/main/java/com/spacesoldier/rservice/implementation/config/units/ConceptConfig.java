package com.spacesoldier.rservice.implementation.config.units;

import com.spacesoldier.rservice.entities.internal.queries.concept.StepFourRequest;
import com.spacesoldier.rservice.entities.internal.queries.concept.StepOneRequest;
import com.spacesoldier.rservice.entities.internal.queries.concept.StepThreeRequest;
import com.spacesoldier.rservice.entities.internal.queries.concept.StepTwoRequest;
import com.spacesoldier.rservice.implementation.execution.logic.units.ConceptUnitImpl;
import com.spacesoldier.rservice.streaming.routing.entities.stream.StreamNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ConceptConfig {

    @Bean
    public StreamNode stepOne(){
        return StreamNode.builder()
                            .streamName("conceptStream")
                            .nodeName("stepOne")
                            .transformationInputType(StepOneRequest.class)
                            .transformationOutputType(StepTwoRequest.class)
                            .transformation(ConceptUnitImpl.conceptStageOne())
                        .build();
    }

    @Bean
    public StreamNode stepTwo(){
        return StreamNode.builder()
                            .streamName("conceptStream")
                            .nodeName("stepTwo")
                            .transformationInputType(StepTwoRequest.class)
                            .transformationOutputType(StepThreeRequest.class)
                            .transformation(ConceptUnitImpl.conceptStageTwo())
                        .build();
    }

    @Bean
    public StreamNode stepThree(){
        return StreamNode.builder()
                                .streamName("conceptStream")
                                .nodeName("stepThree")
                                .transformationInputType(StepThreeRequest.class)
                                .transformationOutputType(StepFourRequest.class)
                                .transformation(ConceptUnitImpl.conceptStageThree())
                            .build();
    }

    @Bean
    public StreamNode stepFour(){
        return StreamNode.builder()
                                .streamName("conceptStream")
                                .nodeName("stepThree")
                                .transformationInputType(StepFourRequest.class)
                                .transformation(ConceptUnitImpl.conceptStageFour())
                            .build();
    }

}
