package com.spacesoldier.rservice.implementation.execution.logic.units;

import com.spacesoldier.rservice.entities.internal.log.LogMessage;
import com.spacesoldier.rservice.entities.internal.queries.concept.StepFourRequest;
import com.spacesoldier.rservice.entities.internal.queries.concept.StepOneRequest;
import com.spacesoldier.rservice.entities.internal.queries.concept.StepThreeRequest;
import com.spacesoldier.rservice.entities.internal.queries.concept.StepTwoRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

@Slf4j
public class ConceptUnitImpl {

    public static final String unitName = "concept-unit";
    private static final Logger logger = LoggerFactory.getLogger("concept");

    public static Function<StepOneRequest, StepTwoRequest> conceptStageOne(){
        return rq -> {
            String stepName = "Concept step 1";
            String logTemplate = "[%s]: process %s";

            log.info(
                    String.format(logTemplate, stepName, "beep")
            );

            return StepTwoRequest.builder()
                    .rqId(rq.getRqId())
                    .build();
        };
    }

    public static Function<StepTwoRequest, StepThreeRequest> conceptStageTwo(){
        return rq -> {
            String stepName = "Concept step 2";
            String logTemplate = "[%s]: process %s";

            log.info(
                    String.format(logTemplate, stepName, "beep")
            );

            return StepThreeRequest.builder()
                    .rqId(rq.getRqId())
                    .build();
        };
    }

    public static Function<StepThreeRequest, StepFourRequest> conceptStageThree(){
        return rq -> {
            String stepName = "Concept step 3";
            String logTemplate = "[%s]: process %s";

            log.info(
                    String.format(logTemplate, stepName, "beep")
            );

            return StepFourRequest.builder()
                    .rqId(rq.getRqId())
                    .build();
        };
    }

    public static Function<StepFourRequest, LogMessage> conceptStageFour(){
        return rq -> {
            String stepName = "Concept step 4";
            String logTemplate = "[%s]: process %s";

            log.info(
                    String.format(logTemplate, stepName, "beep")
            );

            return LogMessage.builder()
                        .rqId(rq.getRqId())
                        .event("request done")
                        .statusCode(0)
                        .statusDesc("request is fully processed")
                    .build();
        };
    }

}
