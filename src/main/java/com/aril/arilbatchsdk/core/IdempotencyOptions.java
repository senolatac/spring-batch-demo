package com.aril.arilbatchsdk.core;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Getter
@Builder
public class IdempotencyOptions implements Serializable {

    @Builder.Default
    private boolean enable = true;

    /***
     * when you send it as null, then default-config (24) will be used.
     */
    private Long ttl;

    /***
     * when you send it as null, then default-config (TimeUnit.HOUR) will be used.
     */
    private TimeUnit ttlTimeUnit;
}
