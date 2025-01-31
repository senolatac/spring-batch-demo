package com.aril.arilbatchsdk.core.item.excel.support;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

@RequiredArgsConstructor
public class DefaultExcelStreamHeaderCallback implements ExcelStreamHeaderCallback {
    private final String[] headerColumns;

    @Override
    public void writeHeader(Row row) {
        for (int i = 0; i < headerColumns.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(headerColumns[i]);
        }
    }
}
