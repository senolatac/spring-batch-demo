package com.aril.arilbatchsdk.core.item.excel.support;

//XLSX files provide better data integrity and recovery capabilities than XLS files.
public enum ExcelFileFormat {
    //XLS is the default file format from Excel 97 to Excel 2003
    XLS,
    //XLSX is the default file format for Excel 2007 and later
    XLSX
}