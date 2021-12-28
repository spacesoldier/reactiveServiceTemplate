package com.spacesoldier.rservice.entities.internal.cache;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class IncomingUpdate {
    private String updateId;        // could possibly be a structure which defines a complex key
    private Object payload;         // exact structure of update payload will be defined later
}
