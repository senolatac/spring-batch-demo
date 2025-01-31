package com.aril.arilbatchsdk.core.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BatchJobItemErrorDetail {

    private String exceptionClass;
    private String message;
}
