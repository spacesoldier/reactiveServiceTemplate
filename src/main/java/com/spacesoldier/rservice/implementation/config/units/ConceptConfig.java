package com.spacesoldier.rservice.implementation.config.units;

import com.spacesoldier.rservice.entities.internal.queries.concept.StepFourRequest;
import com.spacesoldier.rservice.entities.internal.queries.concept.StepOneRequest;
import com.spacesoldier.rservice.entities.internal.queries.concept.StepThreeRequest;
import com.spacesoldier.rservice.entities.internal.queries.concept.StepTwoRequest;
import com.spacesoldier.rservice.implementation.execution.logic.units.ConceptUnitImpl;
import com.spacesoldier.rservice.streaming.routing.ReactiveStreamsBuilder;
import com.spacesoldier.rservice.streaming.routing.entities.stream.StreamNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ConceptConfig {

    @Autowired
    private ReactiveStreamsBuilder streamsBuilder;

    @Bean
    public List<StreamNode> registerConceptNodes(){
        List<StreamNode> nodes = new ArrayList<>(){
            {
                add(
                        streamsBuilder.register(
                                            StepOneRequest.class,
                                            StepTwoRequest.class,
                                            ConceptUnitImpl.conceptStageOne(),
                                            "conceptStream",
                                            "stepOne"
                                        )
                );
                add(
                        streamsBuilder.register(
                                            StepTwoRequest.class,
                                            StepThreeRequest.class,
                                            ConceptUnitImpl.conceptStageTwo(),
                                            "conceptStream",
                                            "stepTwo"
                                        )
                );
                add(
                        streamsBuilder.register(
                                            StepThreeRequest.class,
                                            StepFourRequest.class,
                                            ConceptUnitImpl.conceptStageThree(),
                                            "conceptStream",
                                            "stepThree"
                                        )
                );
                add(
                        streamsBuilder.register(
                                            StepFourRequest.class,
                                            ConceptUnitImpl.conceptStageFour(),
                                            "conceptStream",
                                            "stepFour"
                                        )
                );
            }
        };


        return nodes;
    }
}
