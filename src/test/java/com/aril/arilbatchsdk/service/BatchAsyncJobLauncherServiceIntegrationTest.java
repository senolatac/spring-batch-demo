package com.aril.arilbatchsdk.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Disabled
class BatchAsyncJobLauncherServiceIntegrationTest extends BaseJobLauncherTest {

    @Autowired
    protected BatchAsyncJobLauncherService batchAsyncJobLauncherService;

    @Test
    void asyncBatchCsvToDbCall() throws Exception {
        String id = "test-csv-to-db-async";

        JobExecution jobExecution = batchAsyncJobLauncherService.csvToDbCall(id, inputParamForCsvToDbJob());

        assertAsyncCall(jobExecution, id, false, false);
    }

    @Test
    void asyncBatchCsvToServiceCall() throws Exception {
        String id = "test-csv-to-service-async";

        JobExecution jobExecution = batchAsyncJobLauncherService.csvToServiceCall(id, inputParamForCsvToServiceJob());

        assertAsyncCall(jobExecution, id, false, false);
    }

    @Test
    void asyncBatchCsvToKafkaCall() throws Exception {
        String id = "test-csv-to-kafka-async";

        JobExecution jobExecution = batchAsyncJobLauncherService.csvToKafkaCall(id, inputParamForCsvToKafkaJob());

        assertAsyncCall(jobExecution, id, false, true);
    }

    @Test
    void asyncBatchDbToCsvCall() throws Exception {
        String id = "test-db-to-csv-async";

        JobExecution jobExecution = batchAsyncJobLauncherService.dbToCsvCall(id, inputParamForDbToCsvJob());

        assertAsyncCall(jobExecution, id, true, false);
    }

    @Test
    void asyncBatchDbToExcelCall() throws Exception {
        String id = "test-db-to-excel-async";

        JobExecution jobExecution = batchAsyncJobLauncherService.dbToExcelCall(id, inputParamForDbToExcelJob());

        assertAsyncCall(jobExecution, id, true, false);
    }

    @Test
    void asyncBatchServiceToExcelCall() throws Exception {
        String id = "test-service-to-excel-async";

        JobExecution jobExecution = batchAsyncJobLauncherService.serviceToExcelCall(id, inputParamForServiceToExcelJob());

        assertAsyncCall(jobExecution, id, true, false);
    }

    @Test
    void asyncBatchServiceToKafkaCall() throws Exception {
        String id = "test-service-to-kafka-async";

        JobExecution jobExecution = batchAsyncJobLauncherService.serviceToKafkaCall(id, inputParamForServiceToKafkaJob());

        assertAsyncCall(jobExecution, id, false, true);
    }

    @Test
    void asyncBatchDbToKafkaCall() throws Exception {
        String id = "test-db-to-kafka-async";

        JobExecution jobExecution = batchAsyncJobLauncherService.dbToKafkaCall(id, inputParamForDbToKafkaJob());

        assertAsyncCall(jobExecution, id, false, true);
    }

    @Test
    void asyncBatchExcelToServiceCall() throws Exception {
        String id = "test-excel-to-service-async";

        JobExecution jobExecution = batchAsyncJobLauncherService.excelToServiceCall(id, inputParamForExcelToServiceJob());

        assertSyncCall(jobExecution, id, false, false);
    }
}