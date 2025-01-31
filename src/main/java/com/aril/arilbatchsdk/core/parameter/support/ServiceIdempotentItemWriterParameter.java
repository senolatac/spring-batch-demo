package com.aril.arilbatchsdk.core.parameter.support;

import com.aril.arilbatchsdk.core.item.service.IdempotentService;
import com.aril.valhala.batch.IdempotentBatchItem;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Getter
@Builder
public class ServiceIdempotentItemWriterParameter {

    @NonNull
    private IdempotentService<? extends IdempotentBatchItem> service;

    @NonNull
    private String methodName;

    private List<?> arguments;
}
