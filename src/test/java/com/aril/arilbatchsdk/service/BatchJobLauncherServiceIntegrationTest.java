package com.aril.arilbatchsdk.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Disabled
class BatchJobLauncherServiceIntegrationTest extends BaseJobLauncherTest {

    @Autowired
    private BatchJobLauncherService batchJobLauncherService;

    @Test
    void syncBatchCsvToDbCall() throws Exception {
        String id = "test-csv-to-db-sync";

        JobExecution jobExecution = batchJobLauncherService.csvToDbCall(id, inputParamForCsvToDbJob());

        assertSyncCall(jobExecution, id, false, false);
    }

    @Test
    void syncBatchCsvToServiceCall() throws Exception {
        String id = "test-csv-to-service-async";

        JobExecution jobExecution = batchJobLauncherService.csvToServiceCall(id, inputParamForCsvToServiceJob());

        assertSyncCall(jobExecution, id, false, false);
    }

    @Test
    void syncBatchCsvToKafkaCall() throws Exception {
        String id = "test-csv-to-kafka-async";

        JobExecution jobExecution = batchJobLauncherService.csvToKafkaCall(id, inputParamForCsvToKafkaJob());

        assertSyncCall(jobExecution, id, false, true);
    }

    @Test
    void syncBatchDbToCsvCall() throws Exception {
        String id = "test-db-to-csv-sync";

        JobExecution jobExecution = batchJobLauncherService.dbToCsvCall(id, inputParamForDbToCsvJob());

        assertSyncCall(jobExecution, id, true, false);
    }

    @Test
    void syncBatchDbToExcelCall() throws Exception {
        String id = "test-db-to-excel-sync";

        JobExecution jobExecution = batchJobLauncherService.dbToExcelCall(id, inputParamForDbToExcelJob());

        assertSyncCall(jobExecution, id, true, false);
    }

    @Test
    void syncBatchServiceToExcelCall() throws Exception {
        String id = "test-service-to-excel-sync";

        JobExecution jobExecution = batchJobLauncherService.serviceToExcelCall(id, inputParamForServiceToExcelJob());

        assertSyncCall(jobExecution, id, true, false);
    }

    @Test
    void syncBatchServiceToKafkaCall() throws Exception {
        String id = "test-service-to-kafka-sync";

        JobExecution jobExecution = batchJobLauncherService.serviceToKafkaCall(id, inputParamForServiceToKafkaJob());

        assertSyncCall(jobExecution, id, false, true);
    }

    @Test
    void syncBatchDbToKafkaCall() throws Exception {
        String id = "test-db-to-kafka-sync";

        JobExecution jobExecution = batchJobLauncherService.dbToKafkaCall(id, inputParamForDbToKafkaJob());

        assertSyncCall(jobExecution, id, false, true);
    }

    @Test
    void syncBatchExcelToKafkaCall() throws Exception {
        String id = "test-db-to-kafka-sync";

        JobExecution jobExecution = batchJobLauncherService.excelToKafkaCall(id, inputParamForExcelToKafkaJob());

        assertSyncCall(jobExecution, id, false, true);
    }

    @Test
    void syncBatchExcelToServiceCall() throws Exception {
        String id = "test-excel-to-service-sync";

        JobExecution jobExecution = batchJobLauncherService.excelToServiceCall(id, inputParamForExcelToServiceJob());

        assertSyncCall(jobExecution, id, false, false);
    }

}