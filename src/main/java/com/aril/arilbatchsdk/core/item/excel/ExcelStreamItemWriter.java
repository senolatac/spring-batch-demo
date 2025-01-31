package com.aril.arilbatchsdk.core.item.excel;

import com.aril.arilbatchsdk.core.item.excel.support.*;
import com.aril.arilbatchsdk.core.transform.CompositeFieldExtractor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.fonts.FontCharset;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.WriterNotOpenException;
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;
import org.springframework.batch.item.util.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.io.*;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Setter
public class ExcelStreamItemWriter<T> extends AbstractItemStreamItemWriter<T> implements ResourceAwareItemWriterItemStream<T>, InitializingBean {

    private static final int MAX_DIGIT_PLACE = 2;
    private Resource resource;
    private OutputState state;
    private boolean shouldDeleteIfExists;
    private Integer charset;
    private boolean append;
    private Workbook workbook;
    private String[] columnNames;
    private String sheetName;
    private Sheet sheet;
    private int currentRowIndex;
    private boolean shouldDeleteIfEmpty;
    private ExcelStreamHeaderCallback headerCallback;
    private ExcelStreamFooterCallback footerCallback;
    private ExcelWorkbookFactory workbookFactory;
    private FieldExtractor<T> fieldExtractor;
    private CellStyle cellStyle;
    private NumberFormat cellNumberFormat;
    private ExcelThousandSeparatorFormat thousandSeparatorFormat;

    public ExcelStreamItemWriter() {
        setName(ClassUtils.getShortName(ExcelStreamItemWriter.class));
        this.shouldDeleteIfExists = true;
        this.sheetName = "Auto-Generated";
        this.currentRowIndex = 0;
        this.shouldDeleteIfEmpty = true;
        this.charset = FontCharset.TURKISH.getNativeId();
        this.thousandSeparatorFormat = ExcelThousandSeparatorFormat.DOT;
        this.workbookFactory = new DefaultExcelWorkbookFactory();
    }

    @Override
    public void write(@NotNull Chunk<? extends T> items) throws Exception {
        if (!getOutputState().isInitialized()) {
            throw new WriterNotOpenException("Writer must be open before it can be written to");
        }

        if (log.isDebugEnabled()) {
            log.debug("Writing to file with " + items.size() + " items.");
        }

        OutputState outputState = getOutputState();

        for (T item : items) {
            Row row = createNextRow();
            Object[] cellValues = fieldExtractor.extract(item);
            setCellValues(row, cellValues);
        }
        outputState.setLinesWritten(outputState.getLinesWritten() + items.size());
    }

    private void setCellValues(Row row, Object[] cellValues) {
        for (int i = 0; i < cellValues.length; i++) {
            Object cellValue = cellValues[i];
            Cell cell;
            if (cellValue == null) {
                cell = row.createCell(i, CellType.BLANK);
                cell.setCellStyle(defaultCellStyle());
            } else if (cellValue instanceof Number number) {
                cell = row.createCell(i, CellType.NUMERIC);
                cell.setCellValue(defaultNumberFormat().format(number.doubleValue()));
            } else if (cellValue instanceof Date date) {
                cell = row.createCell(i, CellType.NUMERIC);
                cell.setCellValue(date);
                cell.setCellStyle(defaultCellStyle());
            } else if (cellValue instanceof Calendar calendar) {
                cell = row.createCell(i, CellType.NUMERIC);
                cell.setCellValue(calendar);
                cell.setCellStyle(defaultCellStyle());
            } else if (cellValue instanceof Boolean booleanCellValue) {
                cell = row.createCell(i, CellType.BOOLEAN);
                cell.setCellValue(booleanCellValue);
                cell.setCellStyle(defaultCellStyle());
            } else {
                cell = row.createCell(i, CellType.STRING);
                cell.setCellValue(cellValue.toString());
                cell.setCellStyle(defaultCellStyle());
            }
        }
    }

    private CellStyle defaultCellStyle() {
        if (cellStyle != null) {
            return cellStyle;
        }
        cellStyle = buildCellStyle();
        return cellStyle;
    }

    private NumberFormat defaultNumberFormat() {
        if (cellNumberFormat != null) {
            return cellNumberFormat;
        }
        NumberFormat cellLocaleFormat = NumberFormat.getInstance(thousandSeparatorFormat.getLocale());
        cellLocaleFormat.setMaximumFractionDigits(MAX_DIGIT_PLACE);
        cellNumberFormat = cellLocaleFormat;
        return cellNumberFormat;
    }

    private CellStyle buildCellStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setCharSet(Objects.requireNonNullElse(this.charset, FontCharset.TURKISH.getNativeId()));
        style.setFont(font);
        return style;
    }

    @Override
    public void setResource(@NotNull WritableResource resource) {
        this.resource = resource;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.append) {
            this.shouldDeleteIfExists = false;
        }

        if (columnNames != null) {
            if (headerCallback == null) {
                headerCallback = new DefaultExcelHeaderCallback();
            }
            if (fieldExtractor == null) {
                this.fieldExtractor = new CompositeFieldExtractor<>(columnNames);
            }
        }
        Assert.notNull(fieldExtractor, "An FieldExtractor must be provided.");
    }

    /**
     * Initialize the reader. This method may be called multiple times before close is
     * called.
     */
    @Override
    @SuppressWarnings({"removal"})
    public void open(@NotNull ExecutionContext executionContext) throws ItemStreamException {
        super.open(executionContext);

        Assert.notNull(resource, "The resource must be set");

        if (!getOutputState().isInitialized()) {
            doOpen();
        }
    }

    private void doOpen() throws ItemStreamException {
        OutputState outputState = getOutputState();
        try {
            outputState.initializeBufferedWriter();
        } catch (IOException ioe) {
            throw new ItemStreamException("Failed to initialize writer", ioe);
        }
        if (outputState.lastMarkedByteOffsetPosition == 0 && !outputState.appending
                && headerCallback != null) {
            headerCallback.writeHeader(createNextRow());
        }

    }

    @Override
    @SuppressWarnings({"removal"})
    public void close() throws ItemStreamException {
        if (workbook == null || state == null) {
            return;
        }

        try {
            if (footerCallback != null) {
                footerCallback.writeFooter(createNextRow());
            }
        } finally {
            state.close();
            deleteFileAfterClose();
            state = null;
        }
    }

    private void deleteFileAfterClose() {
        if (state.linesWritten == 0 && shouldDeleteIfEmpty) {
            try {
                Files.delete(resource.getFile().toPath());
            } catch (IOException e) {
                throw new ItemStreamException("Failed to delete empty file on close", e);
            }
        }
    }

    private Row createNextRow() {
        return sheet.createRow(currentRowIndex++);
    }

    // Returns object representing state.
    private OutputState getOutputState() {
        if (state == null) {
            File file;
            try {
                file = resource.getFile();
            } catch (IOException e) {
                throw new ItemStreamException("Could not convert resource to file: [" + resource + "]", e);
            }
            Assert.state(!file.exists() || file.canWrite(), "Resource is not writable: [" + resource + "]");
            state = new OutputState();
            state.setDeleteIfExists(shouldDeleteIfExists);
            state.setAppendAllowed(append);
        }
        return state;
    }

    private class DefaultExcelHeaderCallback implements ExcelStreamHeaderCallback {

        @Override
        public void writeHeader(Row row) {
            for (int i = 0; i < columnNames.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(columnNames[i]);
                cell.setCellStyle(defaultCellStyle());
            }
        }

    }

    @Setter
    @Getter
    private class OutputState {
        private boolean restarted;
        private long lastMarkedByteOffsetPosition;
        private long linesWritten;
        private boolean shouldDeleteIfExists;
        private boolean initialized;
        private boolean append;
        private boolean appending;
        private FileOutputStream os;
        // The bufferedWriter over the file channel that is actually written
        BufferedWriter outputBufferedWriter;

        protected OutputState() {
            this.restarted = false;
            this.lastMarkedByteOffsetPosition = 0L;
            this.linesWritten = 0L;
            this.shouldDeleteIfExists = true;
            this.initialized = false;
            this.append = false;
            this.appending = false;
        }

        public void setAppendAllowed(boolean append) {
            this.append = append;
        }


        public void setDeleteIfExists(boolean shouldDeleteIfExists) {
            this.shouldDeleteIfExists = shouldDeleteIfExists;
        }

        /**
         * Close the open resource and reset counters.
         */
        public void close() {

            initialized = false;
            restarted = false;
            try {
                writeAndCloseWorkbook();
                if (os != null) {
                    os.close();
                }
            } catch (IOException ioe) {
                throw new ItemStreamException("Unable to close the ItemWriter", ioe);
            }
        }

        private void writeAndCloseWorkbook() {
            try (BufferedOutputStream bos = new BufferedOutputStream(this.os)) {
                workbook.write(bos);
                bos.flush();
                closeWorkbook();
            } catch (IOException ex) {
                throw new ItemStreamException("Error writing to output file", ex);
            }
        }

        private void closeWorkbook() throws IOException {
            if (workbook == null) {
                return;
            }
            if (workbook instanceof SXSSFWorkbook sxssfWorkbook) {
                sxssfWorkbook.dispose();
            }
            workbook.close();
        }

        /**
         * Creates the buffered writer for the output file channel based on configuration
         * information.
         *
         * @throws IOException if unable to initialize buffer
         */
        private void initializeBufferedWriter() throws IOException {

            workbook = workbookFactory.create(resource);
            sheet = workbook.createSheet(sheetName);

            File file = resource.getFile();
            FileUtils.setUpOutputFile(file, restarted, append, shouldDeleteIfExists);

            this.os = new FileOutputStream(file.getAbsoluteFile(), true);

            if (append && file.length() > 0) {
                appending = true;
                // Don't write the headers again
            }


            initialized = true;
        }
    }
}
