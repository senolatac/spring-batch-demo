package com.aril.arilbatchsdk.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
class BatchAsyncJobLauncherServiceTest extends BaseMockJobLauncherTest {

    @Autowired
    private BatchAsyncJobLauncherService batchAsyncJobLauncherService;

    @Test
    void asyncBatchCsvToDbCall() throws Exception {
        String id = "test-csv-to-db-async";
        prepareIdempotency();

        JobExecution jobExecution = batchAsyncJobLauncherService.csvToDbCall(id, inputParamForCsvToDbJob());

        assertAsyncCall(jobExecution, id, false, false);
    }

    @Test
    void asyncBatchCsvToServiceCall() throws Exception {
        String id = "test-csv-to-service-async";
        prepareIdempotency();

        JobExecution jobExecution = batchAsyncJobLauncherService.csvToServiceCall(id, inputParamForCsvToServiceJob());

        assertAsyncCall(jobExecution, id, false, false);
    }

    @Test
    void asyncBatchCsvToKafkaCall() throws Exception {
        String id = "test-csv-to-kafka-async";
        prepareKafkaDetails();

        JobExecution jobExecution = batchAsyncJobLauncherService.csvToKafkaCall(id, inputParamForCsvToKafkaJob());

        assertAsyncCall(jobExecution, id, false, true);
        verifyKafkaInteraction();
    }

    @Test
    void asyncBatchDbToServiceCall() throws Exception {
        String id = "test-db-to-service-async";
        prepareIdempotency();

        JobExecution jobExecution = batchAsyncJobLauncherService.dbToServiceCall(id, inputParamForDbToServiceJob());

        assertAsyncCall(jobExecution, id, false, false);
    }

    @Test
    void asyncBatchDbToCsvCall() throws Exception {
        String id = "test-db-to-csv-async";
        prepareMinio(id);

        JobExecution jobExecution = batchAsyncJobLauncherService.dbToCsvCall(id, inputParamForDbToCsvJob());

        assertAsyncCall(jobExecution, id, true, false);
    }

    @Test
    void asyncBatchDbToExcelCall() throws Exception {
        String id = "test-db-to-excel-async";
        prepareMinio(id);

        JobExecution jobExecution = batchAsyncJobLauncherService.dbToExcelCall(id, inputParamForDbToExcelJob());

        assertAsyncCall(jobExecution, id, true, false);
    }

    @Test
    void syncBatchServiceToDbCall() throws Exception {
        String id = "test-service-to-db-async";
        prepareIdempotency();

        JobExecution jobExecution = batchAsyncJobLauncherService.serviceToDbCall(id, inputParamForServiceToDbJob());

        assertAsyncCall(jobExecution, id, false, false);
    }

    @Test
    void asyncBatchServiceToExcelCall() throws Exception {
        String id = "test-service-to-excel-async";
        prepareMinio(id);

        JobExecution jobExecution = batchAsyncJobLauncherService.serviceToExcelCall(id, inputParamForServiceToExcelJob());

        assertAsyncCall(jobExecution, id, true, false);
    }

    @Test
    void asyncBatchControllerToExcelCall() throws Exception {
        String id = "test-controller-to-excel-async";
        prepareMinio(id);

        JobExecution jobExecution = batchAsyncJobLauncherService.controllerToExcelCall(id, inputParamForControllerToExcelJob());

        assertAsyncCall(jobExecution, id, true, false);
    }

    @Test
    void asyncBatchControllerToCsvCall() throws Exception {
        String id = "test-controller-to-csv-async";
        prepareMinio(id);

        JobExecution jobExecution = batchAsyncJobLauncherService.controllerToCsvCall(id, inputParamForControllerToCsvJob());

        assertAsyncCall(jobExecution, id, true, false);
    }

    @Test
    void asyncBatchServiceToKafkaCall() throws Exception {
        String id = "test-service-to-kafka-async";
        prepareKafkaDetails();

        JobExecution jobExecution = batchAsyncJobLauncherService.serviceToKafkaCall(id, inputParamForServiceToKafkaJob());

        assertAsyncCall(jobExecution, id, false, true);
        verifyKafkaInteraction();
    }

    @Test
    void asyncBatchDbToKafkaCall() throws Exception {
        String id = "test-db-to-kafka-async";
        prepareKafkaDetails();

        JobExecution jobExecution = batchAsyncJobLauncherService.dbToKafkaCall(id, inputParamForDbToKafkaJob());

        assertAsyncCall(jobExecution, id, false, true);
        verifyKafkaInteraction();
    }

    @Test
    void asyncBatchExcelToKafkaCall() throws Exception {
        String id = "test-excel-to-kafka-async";
        prepareKafkaDetails();

        JobExecution jobExecution = batchAsyncJobLauncherService.excelToKafkaCall(id, inputParamForExcelToKafkaJob());

        assertAsyncCall(jobExecution, id, false, true);
        verifyKafkaInteraction();
    }

    @Test
    void asyncBatchExcelToDbCall() throws Exception {
        String id = "test-excel-to-db-async";
        prepareIdempotency();

        JobExecution jobExecution = batchAsyncJobLauncherService.excelToDbCall(id, inputParamForExcelToDbJob());

        assertAsyncCall(jobExecution, id, false, false);
    }

    @Test
    void asyncBatchExcelToServiceCall() throws Exception {
        String id = "test-excel-to-service-async";
        prepareIdempotency();

        JobExecution jobExecution = batchAsyncJobLauncherService.excelToServiceCall(id, inputParamForExcelToServiceJob());

        assertAsyncCall(jobExecution, id, false, false);
    }
}