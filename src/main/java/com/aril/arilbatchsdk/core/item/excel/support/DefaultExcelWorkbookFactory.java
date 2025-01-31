package com.aril.arilbatchsdk.core.item.excel.support;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;


public class DefaultExcelWorkbookFactory implements ExcelWorkbookFactory {

    @Override
    public Workbook create(Resource resource) throws IOException {
        ExcelFileFormat ext = fileFormat(resource);

        //XSSFWorkbook: XSSF library provides generating large and small data excel files. The disadvantage of this API is that it keeps all the rows and excel data in heap memory till the final excel file generates.
        //SXSSFWorkbook: SXSSFWorkbook library is based on streaming the data to generate the excel file. It streams the records into the heap memory and the excel file id is placed on the TMP location instead of the heap memory.
        switch (ext) {
            case XLS -> {
                return new HSSFWorkbook();
            }
            case XLSX -> {
                return new SXSSFWorkbook();
            }
            default -> throw new UnsupportedOperationException("Support only *.xls and *.xlsx");
        }
    }

    @Override
    public Workbook createAndRead(Resource resource) throws IOException {
        ExcelFileFormat ext = fileFormat(resource);

        try (InputStream in = resource.getInputStream()) {
            return switch (ext) {
                case XLS -> new HSSFWorkbook(in);
                case XLSX -> new SXSSFWorkbook(new XSSFWorkbook(in));
            };
        }
    }

    @Override
    public ExcelFileFormat fileFormat(Resource resource) {
        return ExcelFileFormat.valueOf(getFileExtension(resource));
    }

    private String getFileExtension(Resource resource) {
        String ext = FilenameUtils.getExtension(resource.getFilename());
        assert ext != null : "File extension cannot be null.";

        return ext.toUpperCase();
    }
}
