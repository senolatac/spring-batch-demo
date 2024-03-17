package com.example.springbatchdemo.config;

import com.example.springbatchdemo.batch.processor.BookAuthorProcessor;
import com.example.springbatchdemo.batch.tasklet.BookTasklet;
import com.example.springbatchdemo.batch.processor.BookTitleProcessor;
import com.example.springbatchdemo.batch.writer.BookWriter;
import com.example.springbatchdemo.entity.BookEntity;
import com.example.springbatchdemo.repository.IBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BatchFromCsvToDbConfig {
    private final IBookRepository bookRepository;

    @Bean
    public Job readBooksFromCsvToDbJob(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       Step chunkStepForCsvToDbJob) {
        return new JobBuilder("readBooksFromCsvToDbJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepForCsvToDbJob)
                .next(taskletStepForCsvToDbJob(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step taskletStepForCsvToDbJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("taskletStepForCsvToDbJob", jobRepository)
                .tasklet(new BookTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step chunkStepForCsvToDbJob(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       FlatFileItemReader<BookEntity> readerForCsvToDbJob) {
        return new StepBuilder("bookReaderStepForCsvToDbJob", jobRepository)
                .<BookEntity, BookEntity>chunk(3, transactionManager)
                .reader(readerForCsvToDbJob)
                .processor(processorForCsvToDbJob())
                .writer(writerForCsvToDbJob())
                .readerIsTransactionalQueue()//to create a reader that does not buffer items in Java
                .taskExecutor(taskExecutorForCsvToDbJob())
                .build();
    }

    /***
     * A spring batch StepScope object is one which is unique to a specific step and not a singleton.
     * As you probably know, the default bean scope in Spring is a singleton.
     * But by specifying a spring batch component being StepScope means
     * that Spring Batch will use the spring container to instantiate
     * a new instance of that component for each step execution.
     */
    @Bean
    public ItemWriter<BookEntity> writerForCsvToDbJob() {
        return new BookWriter(bookRepository);
    }

    @Bean
    public ItemProcessor<BookEntity, BookEntity> processorForCsvToDbJob() {
        return new CompositeItemProcessorBuilder<BookEntity, BookEntity>()
                .delegates(List.of(new BookAuthorProcessor(), new BookTitleProcessor()))
                .build();
    }

    /***
     * The FlatFileItemReader does not load the entire file in memory,
     * it streams data from it chunk by chunk.
     * Items should be garbage collected after each processed chunk.
     * @param resource: custom file input
     * @return FlatFileItemReader
     */
    @Bean
    @StepScope
    public FlatFileItemReader<BookEntity> readerForCsvToDbJob(@Value("#{jobParameters['customFile']}") Resource resource) {
        return new FlatFileItemReaderBuilder<BookEntity>()
                .name("bookReaderForCsvToDbJob")
                .resource(resource)
                .delimited()
                .names("title","author","year")
                .linesToSkip(1)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(BookEntity.class);
                }})
                .build();
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutorForCsvToDbJob() {
        return new ThreadPoolTaskExecutorBuilder()
                .corePoolSize(3)
                .queueCapacity(5)
                .threadNamePrefix("jobForCsvToDbJob")
                .build();
    }
}
