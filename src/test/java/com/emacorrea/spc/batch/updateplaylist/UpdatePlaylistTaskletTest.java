package com.emacorrea.spc.batch.updateplaylist;

import com.emacorrea.spc.config.SpotifyApiConfig;
import com.emacorrea.spc.service.SpotifyApiService;
import org.junit.jupiter.api.*;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;
import spotify.SpotifyTopTracksResponse;
import spotify.SpotifyUpdatePlaylistResponse;

import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = {
        SpotifyApiConfig.class
}, initializers = ConfigDataApplicationContextInitializer.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UpdatePlaylistTaskletTest {

    private static final String PUBSUB_STEP_NAME = "updatePlaylistJob";
    private StepExecution stepExecution;
    private ChunkContext chunkContext;
    private StepContribution contribution;
    private UpdatePlaylistTasklet tasklet;
    private SpotifyApiService spotifyApiService;
    private JobExplorer jobExplorer;
    private JobRepository jobRepository;

    @Autowired
    private SpotifyApiConfig spotifyApiConfig;

    @BeforeEach
    public void beforeEachSetup() {
        stepExecution = MetaDataInstanceFactory.createStepExecution(PUBSUB_STEP_NAME, 1L);
        contribution = new StepContribution(stepExecution);
        chunkContext = new ChunkContext(new StepContext(stepExecution));
        spotifyApiService = mock(SpotifyApiService.class);
        jobExplorer = mock(JobExplorer.class);
        jobRepository = mock(JobRepository.class);

        final SpotifyTopTracksResponse spotifyTopTracksResponse = SpotifyTopTracksResponse.builder()
                .items(new SpotifyTopTracksResponse.Item[]{
                        new SpotifyTopTracksResponse.Item(new SpotifyTopTracksResponse.Artist[]{
                                new SpotifyTopTracksResponse.Artist("testArtistName")
                        }, "testTrackName", "testTrackUri")
                })
                .build();

        when(spotifyApiService.getUsersTopTracks()).thenReturn(Mono.just(spotifyTopTracksResponse));

        tasklet = new UpdatePlaylistTasklet(spotifyApiService);
    }

    @Test
    public void testExecuteUpdatePlaylist() {
        final SpotifyUpdatePlaylistResponse spotifyUpdatePlaylistResponse = SpotifyUpdatePlaylistResponse.builder()
                .snapshotId("idTest")
                .build();

        when(spotifyApiService.updatePlaylist(anyString())).thenReturn(Mono.just(spotifyUpdatePlaylistResponse));

        RepeatStatus actualStatus = tasklet.execute(contribution, chunkContext);
        verify(spotifyApiService, times(1)).updatePlaylist(anyString());
        verify(spotifyApiService, times(1)).getUsersTopTracks();
        Assertions.assertEquals(RepeatStatus.FINISHED, actualStatus);
    }

    @Test
    public void testExecuteGetUsersTopTracksResponseError() {
        doReturn(Mono.error(new Exception("error"))).when(spotifyApiService).getUsersTopTracks();

        RepeatStatus actualStatus = tasklet.execute(contribution, chunkContext);
        verify(spotifyApiService, times(1)).getUsersTopTracks();
        verify(spotifyApiService, never()).updatePlaylist(anyString());
        Assertions.assertEquals(RepeatStatus.FINISHED, actualStatus);
    }

    @Test
    public void testExecuteUpdatePlaylistResponseError() {
        doReturn(Mono.error(new Exception("error"))).when(spotifyApiService).updatePlaylist(anyString());

        RepeatStatus actualStatus = tasklet.execute(contribution, chunkContext);
        verify(spotifyApiService, times(1)).updatePlaylist(anyString());
        verify(spotifyApiService, times(1)).getUsersTopTracks();
        Assertions.assertEquals(RepeatStatus.FINISHED, actualStatus);
    }

}
