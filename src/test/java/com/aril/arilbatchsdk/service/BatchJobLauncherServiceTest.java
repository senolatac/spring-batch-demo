package com.aril.arilbatchsdk.service;

import com.aril.arilbatchsdk.util.ObjectMapperUtils;
import com.aril.arilidempotentsdk.exception.ArilIdempotentException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
class BatchJobLauncherServiceTest extends BaseMockJobLauncherTest {

    @Autowired
    private BatchJobLauncherService batchJobLauncherService;

    @Test
    void syncBatchCsvToDbCall() throws Exception {
        String id = "test-csv-to-db-sync";
        prepareIdempotency();

        JobExecution jobExecution = batchJobLauncherService.csvToDbCall(id, inputParamForCsvToDbJob());

        assertSyncCall(jobExecution, id, false, false);
    }

    @Test
    void syncBatchCsvToServiceCall() throws Exception {
        String id = "test-csv-to-service-async";
        prepareIdempotency();

        JobExecution jobExecution = batchJobLauncherService.csvToServiceCall(id, inputParamForCsvToServiceJob());

        assertSyncCall(jobExecution, id, false, false);
    }

    @Test
    void syncBatchCsvToKafkaCall() throws Exception {
        String id = "test-csv-to-kafka-async";
        prepareKafkaDetails();

        JobExecution jobExecution = batchJobLauncherService.csvToKafkaCall(id, inputParamForCsvToKafkaJob());

        assertSyncCall(jobExecution, id, false, false);
    }

    @Test
    void syncBatchDbToServiceCall() throws Exception {
        String id = "test-db-to-service-sync";
        prepareIdempotency();

        JobExecution jobExecution = batchJobLauncherService.dbToServiceCall(id, inputParamForDbToServiceJob());

        assertSyncCall(jobExecution, id, false, false);
    }

    @Test
    void syncBatchDbToCsvCall() throws Exception {
        String id = "test-db-to-csv-sync";
        prepareMinio(id);

        JobExecution jobExecution = batchJobLauncherService.dbToCsvCall(id, inputParamForDbToCsvJob());

        assertSyncCall(jobExecution, id, true, false);
    }

    @Test
    void syncBatchDbToExcelCall() throws Exception {
        String id = "test-db-to-excel-sync";
        prepareMinio(id);

        JobExecution jobExecution = batchJobLauncherService.dbToExcelCall(id, inputParamForDbToExcelJob());

        assertSyncCall(jobExecution, id, true, false);
    }

    @Test
    void syncBatchServiceToDbCall() throws Exception {
        String id = "test-service-to-db-sync";
        prepareIdempotency();

        JobExecution jobExecution = batchJobLauncherService.serviceToDbCall(id, inputParamForServiceToDbJob());

        assertSyncCall(jobExecution, id, false, false);
    }

    @Test
    void syncBatchServiceToExcelCall() throws Exception {
        String id = "test-service-to-excel-sync";
        prepareMinio(id);

        JobExecution jobExecution = batchJobLauncherService.serviceToExcelCall(id, inputParamForServiceToExcelJob());

        assertSyncCall(jobExecution, id, true, false);
    }

    @Test
    void syncBatchServiceToExcelCall_withMapExtractor() throws Exception {
        String id = "test-service-to-excel-with-map-extractor-sync";
        prepareMinio(id);

        JobExecution jobExecution = batchJobLauncherService.serviceToExcelCall(id, inputParamForServiceToExcelJob(ObjectMapperUtils::toBatchMap));

        assertSyncCall(jobExecution, id, true, false);
    }

    @Test
    void syncBatchControllerToExcelCall() throws Exception {
        String id = "test-controller-to-excel-sync";
        prepareMinio(id);

        JobExecution jobExecution = batchJobLauncherService.controllerToExcelCall(id, inputParamForControllerToExcelJob());

        assertSyncCall(jobExecution, id, true, false);
    }

    @Test
    void syncBatchControllerToExcelCall_withMapExtractor() throws Exception {
        String id = "test-controller-to-excel-with-map-extractor-sync";
        prepareMinio(id);

        JobExecution jobExecution = batchJobLauncherService.controllerToExcelCall(id, inputParamForControllerToExcelJob(ObjectMapperUtils::toBatchMap));

        assertSyncCall(jobExecution, id, true, false);
    }

    @Test
    void syncBatchControllerToCsvCall() throws Exception {
        String id = "test-controller-to-csv-sync";
        prepareMinio(id);

        JobExecution jobExecution = batchJobLauncherService.controllerToCsvCall(id, inputParamForControllerToCsvJob());

        assertSyncCall(jobExecution, id, true, false);
    }

    @Test
    void syncBatchControllerToCsvCall_withMapExtractor() throws Exception {
        String id = "test-controller-to-csv-with-map-extractor-sync";
        prepareMinio(id);

        JobExecution jobExecution = batchJobLauncherService.controllerToCsvCall(id, inputParamForControllerToCsvJob(ObjectMapperUtils::toBatchMap));

        assertSyncCall(jobExecution, id, true, false);
    }

    @Test
    void syncBatchExcelToKafkaCall() throws Exception {
        String id = "test-excel-to-kafka-sync";
        prepareKafkaDetails();

        JobExecution jobExecution = batchJobLauncherService.excelToKafkaCall(id, inputParamForExcelToKafkaJob());

        assertSyncCall(jobExecution, id, false, true);
        verifyKafkaInteraction();
    }

    @Test
    void syncBatchExcelToDbCall() throws Exception {
        String id = "test-excel-to-db-sync";
        prepareIdempotency();

        JobExecution jobExecution = batchJobLauncherService.excelToDbCall(id, inputParamForExcelToDbJob());

        assertSyncCall(jobExecution, id, false, false);
    }

    @Test
    void syncBatchExcelToServiceCall() throws Exception {
        String id = "test-excel-to-service-sync";
        prepareIdempotency();

        JobExecution jobExecution = batchJobLauncherService.excelToServiceCall(id, inputParamForExcelToServiceJob());

        assertSyncCall(jobExecution, id, false, false);
    }

    @Test
    void syncBatchServiceToKafkaCall() throws Exception {
        String id = "test-service-to-kafka-sync";
        prepareKafkaDetails();

        JobExecution jobExecution = batchJobLauncherService.serviceToKafkaCall(id, inputParamForServiceToKafkaJob());

        assertSyncCall(jobExecution, id, false, true);
        verifyKafkaInteraction();
    }

    @Test
    void syncBatchDbToKafkaCall() throws Exception {
        String id = "test-db-to-kafka-sync";
        prepareKafkaDetails();

        JobExecution jobExecution = batchJobLauncherService.dbToKafkaCall(id, inputParamForDbToKafkaJob());

        assertSyncCall(jobExecution, id, false, true);
        assertThat(jobExecution.getStepExecutions().iterator().next().getWriteSkipCount()).isZero();
        assertConsumePercentage(jobExecution.getJobId(), 30);
        verifyKafkaInteraction();
    }

    @Test
    void syncBatchDbToKafkaCall_withUnknownException_failed() throws Exception {
        String id = "test-db-to-kafka-sync";
        prepareKafkaDetails();
        when(idempotentValueOperations.create(any())).thenThrow(RuntimeException.class);

        JobExecution jobExecution = batchJobLauncherService.dbToKafkaCall(id, inputParamForDbToKafkaJob());

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.FAILED);
    }

    @Test
    void syncBatchDbToKafkaCall_withIdempotentException_Skip() throws Exception {
        String id = "test-db-to-kafka-sync";
        prepareKafkaDetails();
        when(idempotentValueOperations.create(any())).thenThrow(ArilIdempotentException.class);

        JobExecution jobExecution = batchJobLauncherService.dbToKafkaCall(id, inputParamForDbToKafkaJob());

        assertSyncCall(jobExecution, id, false, true);
        assertThat(jobExecution.getStepExecutions().iterator().next().getWriteSkipCount()).isEqualTo(100);
        assertConsumePercentage(jobExecution.getJobId(), 100);
        verifyKafkaInteraction();
    }
}