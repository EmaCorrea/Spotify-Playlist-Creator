package com.emacorrea.spc;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackageClasses = { Application.class, AppConstants.class })
@EnableScheduling
public class Application {

	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	Job job;

	public static void main(final String[] args) {
		final var context = SpringApplication.run(Application.class);

		if (isBatchMode(context.getEnvironment())) {
			log.info("Exiting batch mode...");
			System.exit(SpringApplication.exit(context));
		}
	}

	private static boolean isBatchMode(final ConfigurableEnvironment env) {
		final String propertyName = "spring.batch.job.enabled";
		final String propertyValue = env.getProperty(propertyName, "false");
		log.debug("{}: {}", propertyName, propertyValue);
		return Boolean.valueOf(propertyValue);
	}

	//	@Scheduled(cron = "0 */1 * * * ?")
	@Scheduled(cron = "0 0 0 * * FRI")
	@SchedulerLock(name = "updatePlaylist")
	public void updatePlaylist() {
		try {
			log.info("Updating Spotify playlist...");

			JobParameters jobParams = new JobParametersBuilder()
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
