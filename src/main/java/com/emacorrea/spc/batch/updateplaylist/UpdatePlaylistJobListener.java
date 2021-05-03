package com.emacorrea.spc.batch.updateplaylist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class UpdatePlaylistJobListener implements JobExecutionListener {

    private static final String STARTED_STATUS = "STARTED";
    private static final String UNKNOWN_STATUS = "UNKNOWN";
    private static final int MAX_NUMBER_RUNNING_JOBS = 1;

    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;
    private final JobRepository jobRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        final String jobName = jobExecution.getJobInstance().getJobName();
        verifyStalledJobs(jobExecution);
        final Set<JobExecution> executions = jobExplorer.findRunningJobExecutions(jobName);

        if (executions.size() > MAX_NUMBER_RUNNING_JOBS) {
            log.info("Job {} is already running", jobName);
            try {
                jobOperator.stop(jobExecution.getId());
            } catch (NoSuchJobExecutionException | JobExecutionNotRunningException e) {
                log.error("Error stopping job: " + e.getMessage());
            }
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        final String jobName = jobExecution.getJobInstance().getJobName();
        final long elapsedTime = jobExecution.getEndTime().getTime() - jobExecution.getCreateTime().getTime();

        log.info("Job {} completed - status: {}, elapsed time: {}s",
                jobName,
                jobExecution.getExitStatus().getExitCode(),
                TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.MILLISECONDS));
    }

    /**
     * Verifies for any jobs that we're stalled or interrupted during runtime and stops them.
     *
     * @param currentJob current job execution
     */
    private void verifyStalledJobs(JobExecution currentJob) {
        final String jobName = currentJob.getJobInstance().getJobName();
        final Set<JobExecution> executions = jobExplorer.findRunningJobExecutions(jobName);

        for (final JobExecution jobExecution : executions) {
            final String jobStatus = jobExecution.getStatus().toString();
            final String jobExitCode = jobExecution.getExitStatus().getExitCode();

            final LocalDateTime jobStartTime = convertToLocalDateTime(jobExecution.getStartTime());
            final LocalDateTime currentJobStartTime = convertToLocalDateTime(currentJob.getStartTime());
            final Duration jobStartDuration = Duration.between(jobStartTime, currentJobStartTime);

            // If the job has no end time, its status and exit code are set to STARTED and UNKNOWN respectively,
            // and it was started 1 or more hours ago then stop it
            if(STARTED_STATUS.equals(jobStatus) && UNKNOWN_STATUS.equals(jobExitCode) && jobStartDuration.toHours() >= 1) {
                stopJob(jobExecution);
            }
        }
    }

    private void stopJob(JobExecution jobExecution) {
        final String jobName = jobExecution.getJobInstance().getJobName();
        final long jobId = jobExecution.getId();
        final Date jobStartTime = jobExecution.getStartTime();
        final String exitCode = BatchStatus.STOPPED.toString();
        final String exitDescription = "Job was stalled or interrupted and has been stopped.";

        jobExecution.setStatus(BatchStatus.STOPPED);
        jobExecution.setEndTime(new Date());
        jobExecution.setExitStatus(new ExitStatus(exitCode, exitDescription));
        jobRepository.update(jobExecution);

        log.error("Job {} was stalled or interrupted and has been stopped - " +
                        "ID: {}, status: {}, start time: {}, end time: {}",
                jobName,
                jobId,
                jobExecution.getStatus(),
                jobStartTime,
                jobExecution.getEndTime());
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
