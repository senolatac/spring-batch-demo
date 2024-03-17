package com.example.springbatchdemo.batch.processor;

import com.example.springbatchdemo.entity.BookEntity;
import org.springframework.batch.item.ItemProcessor;

public class BookAuthorProcessor implements ItemProcessor<BookEntity, BookEntity> {
    @Override
    public BookEntity process(BookEntity item) throws Exception {
        item.setAuthor("By " + item.getAuthor());
        return item;
    }
}
