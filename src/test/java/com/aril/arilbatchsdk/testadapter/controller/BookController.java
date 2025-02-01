package com.aril.arilbatchsdk.testadapter.controller;

import com.aril.arilbatchsdk.testadapter.entity.BookEntity;
import com.aril.arilbatchsdk.testadapter.repository.BookRepository;
import com.aril.valhala.application.Controller;
import com.aril.valhala.application.PageResult;
import com.aril.valhala.application.Pager;
import com.aril.valhala.dto.SearchResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@org.springframework.stereotype.Controller
@RequiredArgsConstructor
public class BookController extends Controller {
    private final BookRepository bookRepository;

    @PostMapping("filter")
    public ResponseEntity<SearchResultDTO<BookEntity>> find(BookControllerBatchItem request) {
        log.info("Request came with page-request: {}", request.getPager());

        Page<BookEntity> page = bookRepository.findAll(toPageable(request.getPager()));

        SearchResultDTO<BookEntity> result = SearchResultDTO.<BookEntity>builder()
                .result(page.getContent())
                .resultInfo(PageResult.builder()
                        .totalPage(page.getTotalPages())
                        .totalItem(page.getTotalElements())
                        .build())
                .build();
        return ResponseEntity.ok(result);
    }

    private Pageable toPageable(Pager pager) {
        return PageRequest.of(pager.getPageNumber(), pager.getPageSize());
    }
}
