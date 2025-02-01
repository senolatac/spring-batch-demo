package com.aril.arilbatchsdk.testadapter.service;

import com.aril.arilbatchsdk.core.item.service.IdempotentService;
import com.aril.arilbatchsdk.testadapter.entity.BookEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookWriterService implements IdempotentService<BookEntity> {

    public void executeWithParam(int extraParam, BookEntity item) {
        log.info("item writer for id: {} and extra-param: {}", item.getId(), extraParam);
    }
}
