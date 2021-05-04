package com.emacorrea.spc.config;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@Slf4j
public class BatchUpdatePlaylistConfig {

    @Bean
    public BatchUpdater batchUpdater(@Qualifier("jobLauncher") JobLauncher jobLauncher,
                                     @Qualifier("updatePlaylistJob") Job job) {
        return new BatchUpdater(jobLauncher, job);
    }

    private static class BatchUpdater {
        private final JobLauncher jobLauncher;
        private final Job job;

        public BatchUpdater(JobLauncher jobLauncher, Job job) {
            this.jobLauncher = jobLauncher;
            this.job = job;
        }

//        @Scheduled(cron = "0 */1 * * * ?") // Used for testing purposes
	    @Scheduled(cron = "0 0 0 * * FRI")
        @SchedulerLock(name = "updatePlaylist")
        public void updatePlaylist() {
            try {
                log.info("Updating Spotify playlist...");

                final JobParameters jobParams = new JobParametersBuilder()
                        .addString("updatePlaylistJob", String.valueOf(System.currentTimeMillis()))
                        .toJobParameters();
                jobLauncher.run(job, jobParams);

                log.info("Updating Spotify playlist completed");
            }
            catch(JobExecutionException e) {
                log.error("Updating Spotify playlist failed", e);
            }
        }
    }
}
