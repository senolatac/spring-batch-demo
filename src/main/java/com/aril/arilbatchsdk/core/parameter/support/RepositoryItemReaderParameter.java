package com.aril.arilbatchsdk.core.parameter.support;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class RepositoryItemReaderParameter {

    @NonNull
    private PagingAndSortingRepository<?, ?> repository;

    @NonNull
    private String methodName;

    @NonNull
    private Map<String, Sort.Direction> sorts;

    private List<?> arguments;
}
