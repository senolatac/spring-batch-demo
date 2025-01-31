package com.aril.arilbatchsdk.core.result;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.util.concurrent.Callable;

@Getter
@Builder
public class BatchJobItemResultParameter<T extends Serializable> {

    @NonNull
    private Long jobId;

    private String idempotentKey;

    @NonNull
    private Callable<T> callable;

    @Builder.Default
    private int retry = 1;

    /***
     * Save result on db.
     */
    @Builder.Default
    private boolean saveResult = true;

    private String resourceName;

    private String resourceValue;

    /***
     * Show this message on exceptional case, Otherwise set exception message.
     */
    private String errorMessage;
}
