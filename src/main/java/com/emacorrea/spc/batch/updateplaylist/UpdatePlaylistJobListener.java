package com.emacorrea.spc.batch.updateplaylist;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;

@RequiredArgsConstructor
public class UpdatePlaylistJobListener implements JobExecutionListener {

    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {

    }
}
