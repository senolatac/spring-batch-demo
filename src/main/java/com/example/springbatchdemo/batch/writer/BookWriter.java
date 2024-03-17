package com.example.springbatchdemo.batch.writer;

import com.example.springbatchdemo.entity.BookEntity;
import com.example.springbatchdemo.repository.IBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
@RequiredArgsConstructor
public class BookWriter implements ItemWriter<BookEntity> {
    private final IBookRepository bookRepository;

    @Override
    public void write(Chunk<? extends BookEntity> chunk) throws Exception {
        log.info("Writing chunk size: {}", chunk.getItems().size());
        bookRepository.saveAll(chunk.getItems());

    }
}
