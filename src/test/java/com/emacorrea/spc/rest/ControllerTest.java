package com.emacorrea.spc.rest;

import com.emacorrea.spc.exception.*;
import com.emacorrea.spc.service.SpotifyApiService;
import com.emacorrea.spc.spotify.SpotifyPlaylistTracksResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import com.emacorrea.spc.spotify.SpotifyTopTracksResponse;
import com.emacorrea.spc.spotify.SpotifyUpdatePlaylistResponse;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@SpringJUnitConfig({
        Controller.class
})
@WebFluxTest(controllers = Controller.class, excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
@Import(SpotifyApiService.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ControllerTest {

    private static final String SPOTIFY_TRACK_URI = "com.emacorrea.spc.spotify:track:123456789";

    @MockBean
    @Qualifier("asyncJobLauncher")
    private JobLauncher jobLauncher;

    @MockBean
    @Qualifier("updatePlaylistJob")
    @SuppressWarnings("PMD.UnusedPrivateField")
    private Job job;

    @MockBean
    private SpotifyApiService spotifyApiService;

    @Autowired
    private WebTestClient webClient;

    @Test
    public void testGetUsersTopTracks() {
        final SpotifyTopTracksResponse spotifyTopTracksResponse = SpotifyTopTracksResponse.builder()
                .items(new SpotifyTopTracksResponse.Item[]{
                        new SpotifyTopTracksResponse.Item(new SpotifyTopTracksResponse.Artist[]{
                                new SpotifyTopTracksResponse.Artist("testArtistName")
                        }, "testTrackName", "testTrackUri")
                })
                .build();

        when(spotifyApiService.getUsersTopTracks()).thenReturn(Mono.just(spotifyTopTracksResponse));

        webClient.get()
                .uri("/api/v1/toptracks")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items").isNotEmpty()
                .jsonPath("$.items[0].artists").isNotEmpty()
                .jsonPath("$.items[0].artists[0].name").isEqualTo("testArtistName")
                .jsonPath("$.items[0].name").isEqualTo("testTrackName")
                .jsonPath("$.items[0].uri").isEqualTo("testTrackUri");

        verify(spotifyApiService, times(1)).getUsersTopTracks();
    }

    @ParameterizedTest(name = "{displayName} #{index}: {arguments}")
    @MethodSource("testEndpointsArgsProvider")
    public void testGetUsersTopTracksErrorResponses(Mono<Object> exception, HttpStatus httpStatus) {
        doReturn(exception).when(spotifyApiService).getUsersTopTracks();

        webClient.get()
                .uri("/api/v1/toptracks")
                .exchange()
                .expectStatus().isEqualTo(httpStatus);

        verify(spotifyApiService, times(1)).getUsersTopTracks();
    }

    @Test
    public void testUpdatePlaylist() {
        final SpotifyUpdatePlaylistResponse expectedSpotifyUpdatePlaylistResponse = SpotifyUpdatePlaylistResponse.builder()
                .snapshotId("idTest")
                .build();

        when(spotifyApiService.updatePlaylist(anyString())).thenReturn(Mono.just(expectedSpotifyUpdatePlaylistResponse));

        webClient.get()
                .uri(String.format("/api/v1/updateplaylist?itemUris=%s", SPOTIFY_TRACK_URI))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.snapshot_id").isNotEmpty()
                .jsonPath("$.snapshot_id").isEqualTo("idTest");

        verify(spotifyApiService, times(1)).updatePlaylist(anyString());
    }

    @ParameterizedTest(name = "{displayName} #{index}: {arguments}")
    @MethodSource("testEndpointsArgsProvider")
    public void testUpdatePlaylistErrorResponses(Mono<Object> exception, HttpStatus httpStatus) {
        doReturn(exception).when(spotifyApiService).updatePlaylist(anyString());

        webClient.get()
                .uri(String.format("/api/v1/updateplaylist?itemUris=%s", SPOTIFY_TRACK_URI))
                .exchange()
                .expectStatus().isEqualTo(httpStatus);

        verify(spotifyApiService, times(1)).updatePlaylist(anyString());
    }

    @Test
    public void testGetPlaylist() {
        final SpotifyPlaylistTracksResponse spotifyPlaylistTracksResponse = SpotifyPlaylistTracksResponse.builder()
                .items(new SpotifyPlaylistTracksResponse.Item[] {
                        new SpotifyPlaylistTracksResponse.Item(new SpotifyPlaylistTracksResponse.Track(
                                "testTrackName", new SpotifyPlaylistTracksResponse.Artist[]{
                                new SpotifyPlaylistTracksResponse.Artist("testArtistName")
                        }
                        ))
                })
                .build();

        when(spotifyApiService.getPlaylist(anyString())).thenReturn(Mono.just(spotifyPlaylistTracksResponse));

        webClient.get()
                .uri(String.format("/api/v1/playlist?playlistId=%s", "testPlaylistId"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items").isNotEmpty()
                .jsonPath("$.items[0].track").isNotEmpty()
                .jsonPath("$.items[0].track.name").isEqualTo("testTrackName")
                .jsonPath("$.items[0].track.artists").isNotEmpty()
                .jsonPath("$.items[0].track.artists[0].name").isEqualTo("testArtistName");

        verify(spotifyApiService, times(1)).getPlaylist(anyString());
    }

    @ParameterizedTest(name = "{displayName} #{index}: {arguments}")
    @MethodSource("testEndpointsArgsProvider")
    public void testGetPlaylistErrorResponses(Mono<Object> exception, HttpStatus httpStatus) {
        doReturn(exception).when(spotifyApiService).getPlaylist(anyString());

        webClient.get()
                .uri(String.format("/api/v1/playlist?playlistId=%s", "testPlaylistId"))
                .exchange()
                .expectStatus().isEqualTo(httpStatus);

        verify(spotifyApiService, times(1)).getPlaylist(anyString());
    }

    @Test
    public void testLoadUpdatePlaylist() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException {
        final JobExecution expectedJobExecution = MetaDataInstanceFactory.createJobExecution();

        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(expectedJobExecution);

        webClient.post()
                .uri("/api/v1/loadupdateplaylist")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isNotEmpty()
                .jsonPath("$.status").isEqualTo("STARTING")
                .jsonPath("$.exitStatus.exitCode").isNotEmpty()
                .jsonPath("$.exitStatus.exitCode").isEqualTo("UNKNOWN");

        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> testEndpointsArgsProvider() {
        return Stream.of(
                Arguments.arguments(Mono.error(BadRequestException::new), HttpStatus.BAD_REQUEST),
                Arguments.arguments(Mono.error(UnauthorizedException::new), HttpStatus.UNAUTHORIZED),
                Arguments.arguments(Mono.error(ForbiddenException::new), HttpStatus.FORBIDDEN),
                Arguments.arguments(Mono.error(NotFoundException::new), HttpStatus.NOT_FOUND),
                Arguments.arguments(Mono.error(TooManyRequestsException::new), HttpStatus.TOO_MANY_REQUESTS),
                Arguments.arguments(Mono.error(Exception::new), HttpStatus.INTERNAL_SERVER_ERROR)
        );
    }

}
