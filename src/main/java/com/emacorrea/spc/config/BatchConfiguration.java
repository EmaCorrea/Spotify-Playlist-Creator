package com.emacorrea.spc.config;

import com.emacorrea.spc.batch.updateplaylist.UpdatePlaylistJobListener;
import com.emacorrea.spc.batch.updateplaylist.UpdatePlaylistTasklet;
import com.emacorrea.spc.service.SpotifyApiService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobRepository jobRepo;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private SpotifyApiService spotifyApiService;

    @Bean
    public JobLauncher asyncJobLauncher() {
        final SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepo);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return jobLauncher;
    }

    @Bean
    public JobLauncher jobLauncher() {
        final SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepo);
        jobLauncher.setTaskExecutor(new SyncTaskExecutor());
        return jobLauncher;
    }

    @Bean
    public Job updatePlaylistJob() {
        return jobBuilderFactory.get("updatePlaylistJob")
                .incrementer(new RunIdIncrementer())
                .listener(new UpdatePlaylistJobListener(jobExplorer, jobOperator, jobRepo))
                .preventRestart()
                .start(updatePlaylistStep())
                .build();
    }

    @Bean
    public Step updatePlaylistStep() {
        return stepBuilderFactory.get("updatePlaylist")
                .tasklet(updatePlaylistTasklet())
                .build();
    }

    @Bean
    public UpdatePlaylistTasklet updatePlaylistTasklet() {
        return new UpdatePlaylistTasklet(spotifyApiService);
    }

}
