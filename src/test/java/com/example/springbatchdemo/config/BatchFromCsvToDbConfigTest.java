package com.example.springbatchdemo.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest //injects Spring Batch test utilities (such as the JobLauncherTestUtils and JobRepositoryTestUtils) in the test context
//@SpringJUnitConfig //indicates that the class should use Spring’s JUnit facilities
//@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BatchFromCsvToDbConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private Job readBooksFromCsvToDbJob;

    @MockBean
    private JobRepository jobRepository;

    @Value("classpath:book-test-data.csv")
    private Resource customFile;

    @AfterEach
    void tearDown() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    void testJob() throws Exception {
        jobLauncherTestUtils.setJob(readBooksFromCsvToDbJob);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(buildJobParameters());

        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    }

    private JobParameters buildJobParameters() {
        return  new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .addJobParameter("customFile", customFile, Resource.class)
                .toJobParameters();
    }

}