package com.aril.arilbatchsdk.testadapter.service;

import com.aril.arilbatchsdk.core.item.service.reader.PagingReaderService;
import com.aril.arilbatchsdk.testadapter.entity.BookEntity;
import com.aril.arilbatchsdk.testadapter.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookReaderService implements PagingReaderService<BookEntity> {

    private final BookRepository bookRepository;

    @Override
    public Page<BookEntity> findAll(Pageable pageable) {
        log.info("Request came with page-request: {}", pageable);
        return bookRepository.findAll(pageable);
    }
}
