package com.aril.arilbatchsdk.core.item.excel.support;

import org.apache.poi.ss.usermodel.Row;

public interface ExcelStreamFooterCallback {

    /**
     * Write contents to a file using the supplied {@link Row}. It is not
     * required to flush the writer inside this method.
     */
    void writeFooter(Row row);
}
