package com.aril.arilbatchsdk.core.item.service.reader;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PagingReaderService<T> {

    default Page<T> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
