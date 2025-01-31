package com.aril.arilbatchsdk.core.parameter.support;

import com.aril.valhala.batch.IdempotentBatchItem;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class RepositoryIdempotentItemWriterParameter implements Serializable {

    private String methodName;

    private transient List<?> arguments;

    @NonNull
    private transient CrudRepository<? extends IdempotentBatchItem, ?> repository;
}
