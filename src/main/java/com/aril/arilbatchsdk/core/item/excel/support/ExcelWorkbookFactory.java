package com.aril.arilbatchsdk.core.item.excel.support;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.io.Resource;

import java.io.IOException;

public interface ExcelWorkbookFactory {

    Workbook create(Resource resource) throws IOException;

    Workbook createAndRead(Resource resource) throws IOException;

    ExcelFileFormat fileFormat(Resource resource);
}
