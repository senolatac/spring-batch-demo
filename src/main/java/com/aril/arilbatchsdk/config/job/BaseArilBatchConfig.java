package com.aril.arilbatchsdk.config.job;

import com.aril.arilbatchsdk.core.item.excel.ExcelStreamItemWriter;
import com.aril.arilbatchsdk.core.item.excel.ExcelStreamItemWriterBuilder;
import com.aril.arilbatchsdk.core.item.excel.support.DefaultExcelStreamHeaderCallback;
import com.aril.arilbatchsdk.core.item.excel.support.ExcelThousandSeparatorFormat;
import com.aril.arilbatchsdk.core.mapper.IdempotentBatchItemMapFieldSetMapper;
import com.aril.arilbatchsdk.core.parameter.support.RepositoryItemReaderParameter;
import com.aril.arilbatchsdk.core.transform.CompositeFieldExtractor;
import com.aril.arilbatchsdk.util.JobUtils;
import com.aril.valhala.batch.BatchItem;
import com.aril.valhala.batch.IdempotentBatchItem;
import com.aril.valhala.batch.IdempotentBatchItemMap;
import org.springframework.batch.extensions.excel.mapping.BeanWrapperRowMapper;
import org.springframework.batch.extensions.excel.streaming.StreamingXlsxItemReader;
import org.springframework.batch.extensions.excel.support.rowset.DefaultRowSetFactory;
import org.springframework.batch.extensions.excel.support.rowset.StaticColumnNameExtractor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public abstract class BaseArilBatchConfig {

    @SuppressWarnings("unchecked")
    protected <T extends IdempotentBatchItem> FlatFileItemReader<T> configureFlatFileItemReader(String readerName,
                                                                                                Resource resource,
                                                                                                String[] inputFields,
                                                                                                Class<? extends IdempotentBatchItem> inputItemClass) throws Exception {
        return new FlatFileItemReaderBuilder<T>()
                .name(readerName)
                .resource(resource)
                .delimited()
                .delimiter(JobUtils.DELIMITER)
                .names(inputFields)
                .linesToSkip(1)
                .fieldSetMapper((FieldSetMapper<T>) determineFieldSetMapper(inputItemClass))
                .build();
    }

    protected <T> RepositoryItemReader<T> configureRepositoryItemReader(
            String readerName,
            RepositoryItemReaderParameter repositoryItemReader,
            int chunkSize) {
        return new RepositoryItemReaderBuilder<T>()
                .name(readerName)
                .repository(repositoryItemReader.getRepository())
                .methodName(repositoryItemReader.getMethodName())
                .pageSize(chunkSize)
                .arguments(repositoryItemReader.getArguments())
                .sorts(repositoryItemReader.getSorts())
                .build();
    }

    protected <T extends IdempotentBatchItem> StreamingXlsxItemReader<T> configureStreamingXlsxItemReader(String readerName,
                                                                                                          Resource resource,
                                                                                                          String[] inputFields,
                                                                                                          Class<? extends IdempotentBatchItem> inputItemClass) throws Exception {

        StreamingXlsxItemReader<T> reader = new StreamingXlsxItemReader<>();
        reader.setName(readerName);
        DefaultRowSetFactory factory = new DefaultRowSetFactory();
        factory.setColumnNameExtractor(new StaticColumnNameExtractor(inputFields));
        reader.setRowSetFactory(factory);
        reader.setResource(resource);
        reader.setRowMapper(beanWrapperRowMapper(inputItemClass));
        reader.setLinesToSkip(1); // Skip first row as that is the header
        reader.afterPropertiesSet();
        return reader;
    }

    protected <D extends BatchItem> FlatFileItemWriter<D> configureFlatFileItemWriter(String writerName,
                                                                                      String identifier,
                                                                                      String outputHeader,
                                                                                      String[] outputFields) {
        return new FlatFileItemWriterBuilder<D>()
                .name(writerName)
                .resource(new FileSystemResource(JobUtils.getCsvFilenameById(identifier)))
                .headerCallback(writer -> writer.write(outputHeader))
                .lineAggregator(delimitedLineAggregator(outputFields))
                .build();
    }

    protected <D extends BatchItem> ExcelStreamItemWriter<D> configureExcelStreamItemWriter(String writerName,
                                                                                            String identifier,
                                                                                            String[] outputFields,
                                                                                            String[] headerColumns,
                                                                                            ExcelThousandSeparatorFormat thousandSeparatorFormat) {
        return new ExcelStreamItemWriterBuilder<D>()
                .name(writerName)
                .resource(new FileSystemResource(JobUtils.getExcelFilenameById(identifier)))
                .columnNames(outputFields)
                .headerCallback(new DefaultExcelStreamHeaderCallback(headerColumns))
                .thousandSeparatorFormat(thousandSeparatorFormat)
                .build();
    }

    protected <D extends BatchItem> DelimitedLineAggregator<D> delimitedLineAggregator(String[] outputFields) {
        DelimitedLineAggregator<D> delimitedLineAggregator = new DelimitedLineAggregator<>();
        delimitedLineAggregator.setDelimiter(JobUtils.DELIMITER);
        delimitedLineAggregator.setFieldExtractor(new CompositeFieldExtractor<>(outputFields));
        return delimitedLineAggregator;
    }

    private FieldSetMapper<?> determineFieldSetMapper(Class<?> inputItemClass) throws Exception {
        if (IdempotentBatchItemMap.class.isAssignableFrom(inputItemClass)) {
            return new IdempotentBatchItemMapFieldSetMapper();
        }
        return beanWrapperFieldSetMapper(inputItemClass);
    }

    @SuppressWarnings("unchecked")
    private <T extends IdempotentBatchItem> BeanWrapperFieldSetMapper<T> beanWrapperFieldSetMapper(Class<?> inputItemClass) throws Exception {
        BeanWrapperFieldSetMapper<T> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType((Class<? extends T>) inputItemClass);
        beanWrapperFieldSetMapper.afterPropertiesSet();
        return beanWrapperFieldSetMapper;
    }

    @SuppressWarnings("unchecked")
    private <T extends IdempotentBatchItem> BeanWrapperRowMapper<T> beanWrapperRowMapper(Class<? extends IdempotentBatchItem> inputItemClass) throws Exception {
        BeanWrapperRowMapper<T> beanWrapperRowMapper = new BeanWrapperRowMapper<>();
        beanWrapperRowMapper.setTargetType((Class<? extends T>) inputItemClass);
        beanWrapperRowMapper.afterPropertiesSet();
        return beanWrapperRowMapper;
    }
}
