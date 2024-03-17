package com.example.springbatchdemo.batch.tasklet;

import com.example.springbatchdemo.entity.BookEntity;
import com.example.springbatchdemo.repository.IBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@RequiredArgsConstructor
public class BookDataPrepareTasklet implements Tasklet {
    private final IBookRepository bookRepository;

    private static final int DATA_SIZE = 100;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        for (int i = 0; i < DATA_SIZE; i++) {
            BookEntity bookEntity = BookEntity.builder()
                    .author("author " + i)
                    .title("title " + i)
                    .year(i)
                    .build();

            bookRepository.save(bookEntity);
        }
        return RepeatStatus.FINISHED;
    }
}
