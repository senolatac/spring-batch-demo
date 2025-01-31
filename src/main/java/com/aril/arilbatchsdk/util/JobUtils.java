package com.aril.arilbatchsdk.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.JobInstance;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobUtils {
    public static final String FOLDER_FORMAT_FOR_DATE = "yyyy/MM/dd";
    public static final String CSV_FILENAME_FORMAT = "%s.csv";
    public static final String EXCEL_FILENAME_FORMAT = "%s.xlsx";
    public static final String DELIMITER = ";";
    public static final String DEFAULT_JOB_NAME = "default";
    public static final int HUNDRED_PERCENT = 100;

    public static JobInstance getDefaultJobInstance(Long jobId) {
        //job-name is not critical here, use it as default.
        return new JobInstance(jobId, DEFAULT_JOB_NAME);
    }

    public static String getLatestFolder() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(FOLDER_FORMAT_FOR_DATE));
    }

    public static String getCsvFilenameById(String id) {
        return String.format(CSV_FILENAME_FORMAT, id);
    }

    public static String getExcelFilenameById(String id) {
        return String.format(EXCEL_FILENAME_FORMAT, id);
    }
}
