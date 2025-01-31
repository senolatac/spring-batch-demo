package com.aril.arilbatchsdk.core.item.excel;

import com.aril.arilbatchsdk.core.item.excel.support.ExcelStreamFooterCallback;
import com.aril.arilbatchsdk.core.item.excel.support.ExcelStreamHeaderCallback;
import com.aril.arilbatchsdk.core.item.excel.support.ExcelThousandSeparatorFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.core.io.WritableResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ExcelStreamItemWriterBuilder<T> {

    private String name;
    private WritableResource resource;
    private boolean append;
    private boolean shouldDeleteIfExists = true;
    private Integer charset;
    private final List<String> columnNames = new ArrayList<>();
    private String sheetName = "default";
    private boolean shouldDeleteIfEmpty = true;
    private ExcelStreamHeaderCallback headerCallback;
    private ExcelStreamFooterCallback footerCallback;
    private FieldExtractor<T> fieldExtractor;
    private ExcelThousandSeparatorFormat thousandSeparatorFormat = ExcelThousandSeparatorFormat.DOT;

    /**
     * The name used to calculate the key within the
     * {@link org.springframework.batch.item.ExecutionContext}.
     *
     * @param name name of the reader instance
     * @return The current instance of the builder.
     * @see org.springframework.batch.item.ItemStreamSupport#setName(String)
     */
    public ExcelStreamItemWriterBuilder<T> name(String name) {
        this.name = name;

        return this;
    }

    /**
     * The {@link WritableResource} to be used as output.
     *
     * @param resource the output of the writer.
     * @return The current instance of the builder.
     * @see ExcelStreamItemWriter#setResource (WritableResource)
     */
    public ExcelStreamItemWriterBuilder<T> resource(WritableResource resource) {
        this.resource = resource;

        return this;
    }

    /**
     * @param sheetName excel-sheet-name.
     * @return The current instance of the builder.
     * @see ExcelStreamItemWriter#setSheetName (String)
     */
    public ExcelStreamItemWriterBuilder<T> sheetName(String sheetName) {
        this.sheetName = sheetName;

        return this;
    }

    /**
     * Names of each of the fields within the fields that are returned in the order
     * they occur within the formatted file. These names will be used to create a
     * {@link BeanWrapperFieldExtractor} only if no explicit field extractor is set
     *
     * @param names names of each field
     * @return The parent {@link ExcelStreamItemWriterBuilder}
     * @see BeanWrapperFieldExtractor#setNames(String[])
     */
    public ExcelStreamItemWriterBuilder<T> columnNames(String... names) {
        this.columnNames.addAll(Arrays.asList(names));

        return this;
    }

    /**
     * Encoding used for output.
     *
     * @param charset encoding type.
     * @return The current instance of the builder.
     * @see ExcelStreamItemWriter#setCharset(Integer)
     */
    public ExcelStreamItemWriterBuilder<T> encoding(Integer charset) {
        this.charset = charset;

        return this;
    }

    /**
     * Encoding used for output.
     *
     * @param format thousandSeparatorFormat.
     * @return The current instance of the builder.
     * @see ExcelStreamItemWriter#setThousandSeparatorFormat(ExcelThousandSeparatorFormat)
     */
    public ExcelStreamItemWriterBuilder<T> thousandSeparatorFormat(ExcelThousandSeparatorFormat format) {
        this.thousandSeparatorFormat = format;

        return this;
    }

    /**
     * If set to true, once the step is complete, if the resource previously provided is
     * empty, it will be deleted.
     *
     * @param shouldDelete defaults to false
     * @return The current instance of the builder
     * @see ExcelStreamItemWriter#setShouldDeleteIfEmpty(boolean)
     */
    public ExcelStreamItemWriterBuilder<T> shouldDeleteIfEmpty(boolean shouldDelete) {
        this.shouldDeleteIfEmpty = shouldDelete;

        return this;
    }

    /**
     * If set to true, upon the start of the step, if the resource already exists, it will
     * be deleted and recreated.
     *
     * @param shouldDelete defaults to true
     * @return The current instance of the builder
     * @see ExcelStreamItemWriter#setShouldDeleteIfExists(boolean)
     */
    public ExcelStreamItemWriterBuilder<T> shouldDeleteIfExists(boolean shouldDelete) {
        this.shouldDeleteIfExists = shouldDelete;

        return this;
    }

    /**
     * If set to true and the file exists, the output will be appended to the existing
     * file.
     *
     * @param append defaults to false
     * @return The current instance of the builder
     * @see ExcelStreamItemWriter#setAppend (boolean)
     */
    public ExcelStreamItemWriterBuilder<T> append(boolean append) {
        this.append = append;

        return this;
    }

    /**
     * A callback for header processing.
     *
     * @param callback {@link ExcelStreamHeaderCallback} impl
     * @return The current instance of the builder
     * @see ExcelStreamItemWriter#setHeaderCallback(ExcelStreamHeaderCallback)
     */
    public ExcelStreamItemWriterBuilder<T> headerCallback(ExcelStreamHeaderCallback callback) {
        this.headerCallback = callback;

        return this;
    }

    /**
     * A callback for footer processing
     *
     * @param callback {@link ExcelStreamFooterCallback} impl
     * @return The current instance of the builder
     * @see ExcelStreamItemWriter#setFooterCallback(ExcelStreamFooterCallback)
     */
    public ExcelStreamItemWriterBuilder<T> footerCallback(ExcelStreamFooterCallback callback) {
        this.footerCallback = callback;

        return this;
    }

    /**
     * Set the {@link FieldExtractor} to use to extract fields from each item.
     *
     * @param fieldExtractor to use to extract fields from each item
     * @return The current instance of the builder
     */
    public ExcelStreamItemWriterBuilder<T> fieldExtractor(FieldExtractor<T> fieldExtractor) {
        this.fieldExtractor = fieldExtractor;

        return this;
    }

    /**
     * Validates and builds a {@link ExcelStreamItemWriter}.
     *
     * @return a {@link ExcelStreamItemWriter}
     */
    public ExcelStreamItemWriter<T> build() {
        if (this.resource == null) {
            log.debug("The resource is null. This is only a valid scenario when "
                    + "injecting it later as in when using the MultiResourceItemWriter");
        }

        return buildExcelStreamItemWriter();
    }

    private ExcelStreamItemWriter<T> buildExcelStreamItemWriter() {
        ExcelStreamItemWriter<T> writer = new ExcelStreamItemWriter<>();

        writer.setName(this.name);
        writer.setAppend(this.append);
        writer.setCharset(this.charset);
        writer.setFooterCallback(this.footerCallback);
        writer.setHeaderCallback(this.headerCallback);
        writer.setResource(this.resource);
        writer.setShouldDeleteIfEmpty(this.shouldDeleteIfEmpty);
        writer.setShouldDeleteIfExists(this.shouldDeleteIfExists);
        writer.setFieldExtractor(this.fieldExtractor);
        writer.setSheetName(this.sheetName);
        writer.setColumnNames(columnNames.toArray(new String[]{}));
        writer.setThousandSeparatorFormat(Objects.requireNonNullElse(this.thousandSeparatorFormat, ExcelThousandSeparatorFormat.DOT));
        return writer;
    }
}
