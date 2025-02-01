package com.aril.arilbatchsdk.jpa.specification.usecase;

import com.aril.valhala.hexagonal.UseCase;
import com.aril.valhala.product.ModuleType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class FilterBatchJobUseCase extends UseCase {

    private Long startTime;

    private Long endTime;

    private String jobOwner;

    private String jobName;

    @Setter
    private ModuleType module;
}
