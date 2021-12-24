package com.spacesoldier.rservice.entities.internal.queries;


import com.spacesoldier.rservice.entities.io.IncomingRequestEnvelope;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.reactive.function.client.WebClient;

@Data @Builder
public class RunExternalAPICallRequest {
    private String callPath;
    private WebClient.RequestBodySpec requestBodySpec;
    private IncomingRequestEnvelope originalRequest;
}
