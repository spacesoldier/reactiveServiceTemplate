package com.spacesoldier.rservice.entities.internal.log;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class LogMessage {
    private String module;
    private String rqId;
    private int statusCode;
    private String statusDesc;
    private String timestamp;
    private String event;
}
