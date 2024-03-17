package com.example.springbatchdemo.config;

import com.example.springbatchdemo.batch.tasklet.BookDataPrepareTasklet;
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
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class BatchFromDbToCsvConfig {
    private final IBookRepository bookRepository;

    @Bean
    public Job writeBooksFromDbToCsvJob(JobRepository jobRepository, PlatformTransactionManager transactionManager, Step chunkStepForDbToCsvJob) {
        return new JobBuilder("writeBooksFromDbToCsvJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(prepareDataForDbToCsvJob(jobRepository, transactionManager))
                .next(chunkStepForDbToCsvJob)
                .build();
    }

    @Bean
    public Step prepareDataForDbToCsvJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("prepareDataForDbToCsvJob", jobRepository)
                .tasklet(new BookDataPrepareTasklet(bookRepository), transactionManager)
                .build();
    }

    @Bean
    public Step chunkStepForDbToCsvJob(JobRepository jobRepository, PlatformTransactionManager transactionManager, FlatFileItemWriter<BookEntity> writerForDbToCsvJob) {
        return new StepBuilder("bookWriterStepForDbToCsvJob", jobRepository)
                .<BookEntity, BookEntity>chunk(3, transactionManager)
                .reader(bookPaginationReader())
                .processor(processorForDbToCsvJob())
                .writer(writerForDbToCsvJob)
                .readerIsTransactionalQueue()//to create a reader that does not buffer items in Java
                .taskExecutor(taskExecutorForDbToCsvJob())
                .build();
    }

    @Bean
    public ItemReader<BookEntity> bookPaginationReader() {
        return new RepositoryItemReaderBuilder<BookEntity>()
                .name("bookPaginationReader")
                .repository(bookRepository)
                .methodName("findAll")
                .pageSize(3)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<BookEntity,BookEntity> processorForDbToCsvJob(){
        return item -> item;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<BookEntity> writerForDbToCsvJob(@Value("#{jobParameters['outputPath']}") String outputPath) {
        return new FlatFileItemWriterBuilder<BookEntity>()
                .name("writerForDbToCsvJob")
                .resource(new FileSystemResource(outputPath))
                .headerCallback(writer -> writer.write("Id, Title, Author, year"))
                .lineAggregator(new DelimitedLineAggregator<>() {{
                    setDelimiter(",");
                    setFieldExtractor(new BeanWrapperFieldExtractor<>() {{
                        setNames(new String[]{"id", "title", "author", "year"});
                    }});
                }})
                .build();
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutorForDbToCsvJob() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(3);
        threadPoolTaskExecutor.setQueueCapacity(5);
        threadPoolTaskExecutor.setThreadNamePrefix("jobForDbToCsvJob");
        threadPoolTaskExecutor.afterPropertiesSet();
        return threadPoolTaskExecutor;
    }
}
