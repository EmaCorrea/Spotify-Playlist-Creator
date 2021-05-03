package com.emacorrea.spc.batch.updateplaylist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.MetaDataInstanceFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UpdatePlaylistJobListenerTest {
    private UpdatePlaylistJobListener updatePlaylistJobListener;
    private JobExecution jobExecution;
    private JobExplorer jobExplorer;
    private JobOperator jobOperator;

    @BeforeEach
    public void beforeEachSetup() {
        jobExecution = MetaDataInstanceFactory.createJobExecution();
        jobExplorer = mock(JobExplorer.class);
        jobOperator = mock(JobOperator.class);
        final JobRepository jobRepository = mock(JobRepository.class);

        final JobInstance jobInstance = new JobInstance(1L, "updatePlaylistJob");

        jobExecution.setJobInstance(jobInstance);
        jobExecution.setStartTime(new Date());
        jobExecution.setCreateTime(new Date());
        jobExecution.setEndTime(new Date());

        updatePlaylistJobListener = new UpdatePlaylistJobListener(jobExplorer, jobOperator, jobRepository);
    }

    @Test
    public void testNoRunningJobs() {
        updatePlaylistJobListener.beforeJob(jobExecution);
        updatePlaylistJobListener.afterJob(jobExecution);
        assertEquals(BatchStatus.STARTING, jobExecution.getStatus());
    }

    @Test
    public void testStoppedCurrentJob() {
        JobExecution jobExecution1 = MetaDataInstanceFactory.createJobExecution();
        JobExecution jobExecution2 = MetaDataInstanceFactory.createJobExecution();

        jobExecution1.setId(1L);
        jobExecution1.setStartTime(new Date());

        jobExecution2.setId(2L);
        jobExecution2.setStartTime(new Date());

        final Set<JobExecution> jobExecutions = Set.of(
                jobExecution1,
                jobExecution2
        );

        when(jobExplorer.findRunningJobExecutions(anyString())).thenReturn(jobExecutions);

        updatePlaylistJobListener.beforeJob(jobExecution);
        updatePlaylistJobListener.afterJob(jobExecution);

        assertEquals(BatchStatus.STARTING, jobExecution.getStatus());
    }

    @Test
    public void testStoppedCurrentJobException() throws NoSuchJobExecutionException, JobExecutionNotRunningException {
        JobExecution jobExecution1 = MetaDataInstanceFactory.createJobExecution();
        JobExecution jobExecution2 = MetaDataInstanceFactory.createJobExecution();

        jobExecution1.setId(1L);
        jobExecution1.setStartTime(new Date());

        jobExecution2.setId(2L);
        jobExecution2.setStartTime(new Date());

        final Set<JobExecution> jobExecutions = Set.of(
                jobExecution1,
                jobExecution2
        );

        when(jobExplorer.findRunningJobExecutions(anyString())).thenReturn(jobExecutions);
        doThrow(new NoSuchJobExecutionException("error")).when(jobOperator).stop(anyLong());

        updatePlaylistJobListener.beforeJob(jobExecution);
        updatePlaylistJobListener.afterJob(jobExecution);

        assertEquals(BatchStatus.STARTING, jobExecution.getStatus());

        doThrow(new JobExecutionNotRunningException("error")).when(jobOperator).stop(anyLong());

        updatePlaylistJobListener.beforeJob(jobExecution);
        updatePlaylistJobListener.afterJob(jobExecution);

        assertEquals(BatchStatus.STARTING, jobExecution.getStatus());
    }

    @Test
    public void testStoppedStalledJob() {
        JobExecution jobExecution1 = MetaDataInstanceFactory.createJobExecution();
        JobExecution jobExecution2 = MetaDataInstanceFactory.createJobExecution();

        jobExecution1.setId(1L);
        jobExecution1.setStartTime(new Date());

        jobExecution2.setId(2L);
        jobExecution2.setStartTime(
                Date.from(
                        LocalDateTime.now()
                                .minusHours(2)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                )
        );
        jobExecution2.setStatus(BatchStatus.STARTED);
        jobExecution2.setExitStatus(ExitStatus.UNKNOWN);

        final Set<JobExecution> jobExecutions = Set.of(
                jobExecution1,
                jobExecution2
        );

        when(jobExplorer.findRunningJobExecutions(anyString())).thenReturn(jobExecutions);

        updatePlaylistJobListener.beforeJob(jobExecution);
        updatePlaylistJobListener.afterJob(jobExecution);

        assertEquals(ExitStatus.UNKNOWN, jobExecution1.getExitStatus());
        assertTrue("STOPPED".equals(jobExecution2.getExitStatus().getExitCode()) &&
                "Job was stalled or interrupted and has been stopped.".equals(jobExecution2.getExitStatus().getExitDescription()));
    }

}
