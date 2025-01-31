package com.aril.arilbatchsdk.service;

import com.aril.arilbatchsdk.config.ArilBatchConfigConstants;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class BatchAsyncJobLauncherService extends BaseBatchJobLauncherService {

    @Qualifier(ArilBatchConfigConstants.ASYNC_JOB_LAUNCHER)
    private final JobLauncher asyncJobLauncher;

    protected BatchAsyncJobLauncherService(@Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_CSV_TO_DB) Job jobForCsvToDb,
                                           @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_CSV_TO_SERVICE) Job jobForCsvToService,
                                           @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_CSV_TO_KAFKA) Job jobForCsvToKafka,
                                           @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_DB_TO_CSV) Job jobForDbToCsv,
                                           @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_DB_TO_EXCEL) Job jobForDbToExcel,
                                           @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_DB_TO_SERVICE) Job jobForDbToService,
                                           @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_SERVICE_TO_EXCEL) Job jobForServiceToExcel,
                                           @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_CONTROLLER_TO_EXCEL) Job jobForControllerToExcel,
                                           @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_CONTROLLER_TO_CSV) Job jobForControllerToCsv,
                                           @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_SERVICE_TO_KAFKA) Job jobForServiceToKafka,
                                           @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_DB_TO_KAFKA) Job jobForDbToKafka,
                                           @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_EXCEL_TO_KAFKA) Job jobForExcelToKafka,
                                           @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_EXCEL_TO_DB) Job jobForExcelToDb,
                                           @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_EXCEL_TO_SERVICE) Job jobForExcelToService,
                                           @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_SERVICE_TO_DB) Job jobForServiceToDb,
                                           JobLauncher asyncJobLauncher) {
        super(jobForCsvToDb, jobForCsvToService, jobForCsvToKafka, jobForDbToCsv, jobForDbToExcel, jobForDbToService, jobForServiceToExcel, jobForControllerToExcel, jobForControllerToCsv, jobForServiceToKafka, jobForDbToKafka, jobForExcelToKafka, jobForExcelToDb, jobForExcelToService, jobForServiceToDb);
        this.asyncJobLauncher = asyncJobLauncher;
    }

    @Override
    protected JobLauncher jobLauncher() {
        return asyncJobLauncher;
    }
}
