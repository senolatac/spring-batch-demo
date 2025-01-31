package com.aril.arilbatchsdk.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArilBatchConfigConstants {
    public static final String INPUT_PARAM = "inputParam";
    public static final String INPUT_PARAM_IDENTIFIER = "inputParamId";
    public static final String JOB_EXPORT_FOLDER = "jobExportFolder";
    public static final String JOB_FOR_CSV_TO_DB = "jobForCsvToDb";
    public static final String JOB_FOR_CSV_TO_SERVICE = "jobForCsvToService";
    public static final String JOB_FOR_CSV_TO_KAFKA = "jobForCsvToKafka";
    public static final String JOB_FOR_DB_TO_CSV = "jobForDbToCsv";
    public static final String JOB_FOR_DB_TO_EXCEL = "jobForDbToExcel";
    public static final String JOB_FOR_SERVICE_TO_EXCEL = "jobForServiceToExcel";
    public static final String JOB_FOR_CONTROLLER_TO_EXCEL = "jobForControllerToExcel";
    public static final String JOB_FOR_CONTROLLER_TO_CSV = "jobForControllerToCsv";
    public static final String JOB_FOR_SERVICE_TO_KAFKA = "jobForServiceToKafka";
    public static final String JOB_FOR_SERVICE_TO_DB = "jobForServiceToDb";
    public static final String JOB_FOR_EXCEL_TO_KAFKA = "jobForExcelToKafka";
    public static final String JOB_FOR_EXCEL_TO_DB = "jobForExcelToDb";
    public static final String JOB_FOR_EXCEL_TO_SERVICE = "jobForExcelToService";
    public static final String JOB_FOR_DB_TO_KAFKA = "jobForDbToKafka";
    public static final String JOB_FOR_DB_TO_SERVICE = "jobForDbToService";
    public static final String ASYNC_JOB_LAUNCHER = "asyncJobLauncher";
}
