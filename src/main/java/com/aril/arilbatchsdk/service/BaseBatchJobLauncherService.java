package com.aril.arilbatchsdk.service;

import com.aril.arilbatchsdk.config.ArilBatchConfigConstants;
import com.aril.arilbatchsdk.core.parameter.*;
import com.aril.arilbatchsdk.core.parameter.support.BatchJobParameterWrapper;
import com.aril.arilbatchsdk.util.JobUtils;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.UUID;

public abstract class BaseBatchJobLauncherService {
    protected final Job jobForCsvToDb;
    protected final Job jobForCsvToKafka;
    protected final Job jobForCsvToService;
    protected final Job jobForDbToCsv;
    protected final Job jobForDbToExcel;
    protected final Job jobForDbToService;
    protected final Job jobForServiceToExcel;
    protected final Job jobForControllerToExcel;
    protected final Job jobForControllerToCsv;
    protected final Job jobForServiceToKafka;
    protected final Job jobForDbToKafka;
    protected final Job jobForExcelToKafka;
    protected final Job jobForExcelToDb;
    protected final Job jobForExcelToService;
    protected final Job jobForServiceToDb;

    @SuppressWarnings("java:S107")
    protected BaseBatchJobLauncherService(
            @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_CSV_TO_DB) Job jobForCsvToDb,
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
            @Autowired(required = false) @Qualifier(ArilBatchConfigConstants.JOB_FOR_SERVICE_TO_DB) Job jobForServiceToDb) {
        this.jobForCsvToDb = jobForCsvToDb;
        this.jobForCsvToService = jobForCsvToService;
        this.jobForCsvToKafka = jobForCsvToKafka;
        this.jobForDbToCsv = jobForDbToCsv;
        this.jobForDbToExcel = jobForDbToExcel;
        this.jobForDbToService = jobForDbToService;
        this.jobForServiceToExcel = jobForServiceToExcel;
        this.jobForControllerToExcel = jobForControllerToExcel;
        this.jobForControllerToCsv = jobForControllerToCsv;
        this.jobForServiceToKafka = jobForServiceToKafka;
        this.jobForDbToKafka = jobForDbToKafka;
        this.jobForExcelToKafka = jobForExcelToKafka;
        this.jobForExcelToDb = jobForExcelToDb;
        this.jobForExcelToService = jobForExcelToService;
        this.jobForServiceToDb = jobForServiceToDb;
    }

    public JobExecution dbToCsvCall(BatchDbToCsvJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return dbToCsvCall(UUID.randomUUID().toString(), param);
    }

    public JobExecution dbToCsvCall(String identifier, BatchDbToCsvJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobLauncher().run(jobForDbToCsv, buildParamsForBatchDbToCsv(identifier, param));
    }

    public JobExecution dbToExcelCall(BatchDbToExcelJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return dbToExcelCall(UUID.randomUUID().toString(), param);
    }

    public JobExecution dbToExcelCall(String identifier, BatchDbToExcelJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobLauncher().run(jobForDbToExcel, buildParamsForBatchDbToExcel(identifier, param));
    }

    public JobExecution dbToServiceCall(BatchDbToServiceJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return dbToServiceCall(UUID.randomUUID().toString(), param);
    }

    public JobExecution dbToServiceCall(String identifier, BatchDbToServiceJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobLauncher().run(jobForDbToService, buildParamsForBatchDbToService(identifier, param));
    }

    public JobExecution serviceToExcelCall(BatchServiceToExcelJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return serviceToExcelCall(UUID.randomUUID().toString(), param);
    }

    public JobExecution serviceToExcelCall(String identifier, BatchServiceToExcelJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobLauncher().run(jobForServiceToExcel, buildParamsForBatchServiceToExcel(identifier, param));
    }

    public JobExecution serviceToDbCall(BatchServiceToDbJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return serviceToDbCall(UUID.randomUUID().toString(), param);
    }

    public JobExecution serviceToDbCall(String identifier, BatchServiceToDbJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobLauncher().run(jobForServiceToDb, buildParamsForBatchServiceToDb(identifier, param));
    }

    public JobExecution controllerToExcelCall(BatchControllerToExcelJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return controllerToExcelCall(UUID.randomUUID().toString(), param);
    }

    public JobExecution controllerToExcelCall(String identifier, BatchControllerToExcelJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobLauncher().run(jobForControllerToExcel, buildParamsForBatchControllerToExcel(identifier, param));
    }

    public JobExecution controllerToCsvCall(BatchControllerToCsvJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return controllerToCsvCall(UUID.randomUUID().toString(), param);
    }

    public JobExecution controllerToCsvCall(String identifier, BatchControllerToCsvJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobLauncher().run(jobForControllerToCsv, buildParamsForBatchControllerToCsv(identifier, param));
    }

    public JobExecution serviceToKafkaCall(BatchServiceToKafkaJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return serviceToKafkaCall(UUID.randomUUID().toString(), param);
    }

    public JobExecution serviceToKafkaCall(String identifier, BatchServiceToKafkaJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobLauncher().run(jobForServiceToKafka, buildParamsForBatchServiceToKafka(identifier, param));
    }

    public JobExecution dbToKafkaCall(BatchDbToKafkaJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return dbToKafkaCall(UUID.randomUUID().toString(), param);
    }

    public JobExecution dbToKafkaCall(String identifier, BatchDbToKafkaJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobLauncher().run(jobForDbToKafka, buildParamsForBatchDbToKafka(identifier, param));
    }

    public JobExecution csvToDbCall(BatchCsvToDbJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return csvToDbCall(UUID.randomUUID().toString(), param);
    }

    public JobExecution csvToDbCall(String identifier, BatchCsvToDbJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobLauncher().run(jobForCsvToDb, buildParamsForBatchCsvToDb(identifier, param));
    }

    public JobExecution csvToServiceCall(BatchCsvToServiceJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return csvToServiceCall(UUID.randomUUID().toString(), param);
    }

    public JobExecution csvToServiceCall(String identifier, BatchCsvToServiceJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobLauncher().run(jobForCsvToService, buildParamsForBatchCsvToService(identifier, param));
    }

    public JobExecution csvToKafkaCall(BatchCsvToKafkaJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return csvToKafkaCall(UUID.randomUUID().toString(), param);
    }

    public JobExecution csvToKafkaCall(String identifier, BatchCsvToKafkaJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobLauncher().run(jobForCsvToKafka, buildParamsForBatchCsvToKafka(identifier, param));
    }

    public JobExecution excelToKafkaCall(BatchExcelToKafkaJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return excelToKafkaCall(UUID.randomUUID().toString(), param);
    }

    public JobExecution excelToKafkaCall(String identifier, BatchExcelToKafkaJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobLauncher().run(jobForExcelToKafka, buildParamsForBatchExcelToKafka(identifier, param));
    }

    public JobExecution excelToDbCall(BatchExcelToDbJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return excelToDbCall(UUID.randomUUID().toString(), param);
    }

    public JobExecution excelToDbCall(String identifier, BatchExcelToDbJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobLauncher().run(jobForExcelToDb, buildParamsForBatchExcelToDb(identifier, param));
    }

    public JobExecution excelToServiceCall(BatchExcelToServiceJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return excelToServiceCall(UUID.randomUUID().toString(), param);
    }

    public JobExecution excelToServiceCall(String identifier, BatchExcelToServiceJobParameter param) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobLauncher().run(jobForExcelToService, buildParamsForBatchExcelToService(identifier, param));
    }

    protected abstract JobLauncher jobLauncher();

    protected JobParameters buildParamsForBatchDbToCsv(String identifier, BatchDbToCsvJobParameter param) {
        return new JobParametersBuilder()
                .addString(ArilBatchConfigConstants.INPUT_PARAM_IDENTIFIER, identifier)
                .addString(ArilBatchConfigConstants.JOB_EXPORT_FOLDER, JobUtils.getLatestFolder(), false)
                .addJobParameter(ArilBatchConfigConstants.INPUT_PARAM, createJobParamWrapper(param))
                .toJobParameters();
    }

    protected JobParameters buildParamsForBatchDbToExcel(String identifier, BatchDbToExcelJobParameter param) {
        return new JobParametersBuilder()
                .addString(ArilBatchConfigConstants.INPUT_PARAM_IDENTIFIER, identifier)
                .addString(ArilBatchConfigConstants.JOB_EXPORT_FOLDER, JobUtils.getLatestFolder(), false)
                .addJobParameter(ArilBatchConfigConstants.INPUT_PARAM, createJobParamWrapper(param))
                .toJobParameters();
    }

    protected JobParameters buildParamsForBatchDbToService(String identifier, BatchDbToServiceJobParameter param) {
        return new JobParametersBuilder()
                .addString(ArilBatchConfigConstants.INPUT_PARAM_IDENTIFIER, identifier)
                .addString(ArilBatchConfigConstants.JOB_EXPORT_FOLDER, JobUtils.getLatestFolder(), false)
                .addJobParameter(ArilBatchConfigConstants.INPUT_PARAM, createJobParamWrapper(param))
                .toJobParameters();
    }

    protected JobParameters buildParamsForBatchServiceToDb(String identifier, BatchServiceToDbJobParameter param) {
        return new JobParametersBuilder()
                .addString(ArilBatchConfigConstants.INPUT_PARAM_IDENTIFIER, identifier)
                .addJobParameter(ArilBatchConfigConstants.INPUT_PARAM, createJobParamWrapper(param))
                .toJobParameters();
    }

    protected JobParameters buildParamsForBatchServiceToExcel(String identifier, BatchServiceToExcelJobParameter param) {
        return new JobParametersBuilder()
                .addString(ArilBatchConfigConstants.INPUT_PARAM_IDENTIFIER, identifier)
                .addString(ArilBatchConfigConstants.JOB_EXPORT_FOLDER, JobUtils.getLatestFolder(), false)
                .addJobParameter(ArilBatchConfigConstants.INPUT_PARAM, createJobParamWrapper(param))
                .toJobParameters();
    }

    protected JobParameters buildParamsForBatchControllerToExcel(String identifier, BatchControllerToExcelJobParameter param) {
        return new JobParametersBuilder()
                .addString(ArilBatchConfigConstants.INPUT_PARAM_IDENTIFIER, identifier)
                .addString(ArilBatchConfigConstants.JOB_EXPORT_FOLDER, JobUtils.getLatestFolder(), false)
                .addJobParameter(ArilBatchConfigConstants.INPUT_PARAM, createJobParamWrapper(param))
                .toJobParameters();
    }

    protected JobParameters buildParamsForBatchControllerToCsv(String identifier, BatchControllerToCsvJobParameter param) {
        return new JobParametersBuilder()
                .addString(ArilBatchConfigConstants.INPUT_PARAM_IDENTIFIER, identifier)
                .addString(ArilBatchConfigConstants.JOB_EXPORT_FOLDER, JobUtils.getLatestFolder(), false)
                .addJobParameter(ArilBatchConfigConstants.INPUT_PARAM, createJobParamWrapper(param))
                .toJobParameters();
    }

    protected JobParameters buildParamsForBatchServiceToKafka(String identifier, BatchServiceToKafkaJobParameter param) {
        return new JobParametersBuilder()
                .addString(ArilBatchConfigConstants.INPUT_PARAM_IDENTIFIER, identifier)
                .addJobParameter(ArilBatchConfigConstants.INPUT_PARAM, createJobParamWrapper(param))
                .toJobParameters();
    }

    protected JobParameters buildParamsForBatchDbToKafka(String identifier, BatchDbToKafkaJobParameter param) {
        return new JobParametersBuilder()
                .addString(ArilBatchConfigConstants.INPUT_PARAM_IDENTIFIER, identifier)
                .addJobParameter(ArilBatchConfigConstants.INPUT_PARAM, createJobParamWrapper(param))
                .toJobParameters();
    }

    protected JobParameters buildParamsForBatchCsvToDb(String identifier, BatchCsvToDbJobParameter param) {
        return new JobParametersBuilder()
                .addString(ArilBatchConfigConstants.INPUT_PARAM_IDENTIFIER, identifier)
                .addJobParameter(ArilBatchConfigConstants.INPUT_PARAM, createJobParamWrapper(param))
                .toJobParameters();
    }

    protected JobParameters buildParamsForBatchCsvToService(String identifier, BatchCsvToServiceJobParameter param) {
        return new JobParametersBuilder()
                .addString(ArilBatchConfigConstants.INPUT_PARAM_IDENTIFIER, identifier)
                .addString(ArilBatchConfigConstants.JOB_EXPORT_FOLDER, JobUtils.getLatestFolder(), false)
                .addJobParameter(ArilBatchConfigConstants.INPUT_PARAM, createJobParamWrapper(param))
                .toJobParameters();
    }

    protected JobParameters buildParamsForBatchCsvToKafka(String identifier, BatchCsvToKafkaJobParameter param) {
        return new JobParametersBuilder()
                .addString(ArilBatchConfigConstants.INPUT_PARAM_IDENTIFIER, identifier)
                .addJobParameter(ArilBatchConfigConstants.INPUT_PARAM, createJobParamWrapper(param))
                .toJobParameters();
    }

    protected JobParameters buildParamsForBatchExcelToKafka(String identifier, BatchExcelToKafkaJobParameter param) {
        return new JobParametersBuilder()
                .addString(ArilBatchConfigConstants.INPUT_PARAM_IDENTIFIER, identifier)
                .addJobParameter(ArilBatchConfigConstants.INPUT_PARAM, createJobParamWrapper(param))
                .toJobParameters();
    }

    protected JobParameters buildParamsForBatchExcelToDb(String identifier, BatchExcelToDbJobParameter param) {
        return new JobParametersBuilder()
                .addString(ArilBatchConfigConstants.INPUT_PARAM_IDENTIFIER, identifier)
                .addJobParameter(ArilBatchConfigConstants.INPUT_PARAM, createJobParamWrapper(param))
                .toJobParameters();
    }

    protected JobParameters buildParamsForBatchExcelToService(String identifier, BatchExcelToServiceJobParameter param) {
        return new JobParametersBuilder()
                .addString(ArilBatchConfigConstants.INPUT_PARAM_IDENTIFIER, identifier)
                .addString(ArilBatchConfigConstants.JOB_EXPORT_FOLDER, JobUtils.getLatestFolder(), false)
                .addJobParameter(ArilBatchConfigConstants.INPUT_PARAM, createJobParamWrapper(param))
                .toJobParameters();
    }

    @SuppressWarnings({"unchecked", "java:S3740", "rawtypes"})
    private <T extends BaseBatchJobParameter> JobParameter<BatchJobParameterWrapper<T>> createJobParamWrapper(T param) {
        return new JobParameter(new BatchJobParameterWrapper<>(param), BatchJobParameterWrapper.class, false);
    }
}
