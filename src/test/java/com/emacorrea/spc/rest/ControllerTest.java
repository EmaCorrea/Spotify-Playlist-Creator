package com.emacorrea.spc.rest;

import com.emacorrea.spc.exception.BadRequestException;
import com.emacorrea.spc.service.SpotifyApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import spotify.SpotifyTopTracksResponse;
import spotify.SpotifyUpdatePlaylistResponse;

import static org.mockito.Mockito.*;

@SpringJUnitConfig({
        Controller.class
})
@WebFluxTest(controllers = Controller.class, excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
@Import(SpotifyApiService.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ControllerTest {

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

    @Test
    public void testGetUsersTopTracksNotFoundError() {
        webClient.get()
                .uri("/api/v1/toptracks1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);

        verify(spotifyApiService, never()).getUsersTopTracks();
    }

    @Test
    public void testGetUsersTopTracksInternalServerError() {
        webClient.get()
                .uri("/api/v1/toptracks")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        verify(spotifyApiService, times(1)).getUsersTopTracks();
    }

    @Test
    public void testUpdatePlaylist() {
        final SpotifyUpdatePlaylistResponse expectedSpotifyUpdatePlaylistResponse = SpotifyUpdatePlaylistResponse.builder()
                .snapshotId("idTest")
                .build();

        when(spotifyApiService.updatePlaylist(anyString())).thenReturn(Mono.just(expectedSpotifyUpdatePlaylistResponse));

        webClient.get()
                .uri(String.format("/api/v1/updateplaylist?itemUris=%s", "spotify:track:123456789"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.snapshot_id").isNotEmpty()
                .jsonPath("$.snapshot_id").isEqualTo("idTest");

        verify(spotifyApiService, times(1)).updatePlaylist(anyString());
    }

    @Test
    public void testUpdatePlaylistNotFoundError() {
        webClient.get()
                .uri(String.format("/api/v1/updateplaylist1?itemUris=%s", "spotify:track:123456789"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);

        verify(spotifyApiService, never()).updatePlaylist(anyString());
    }

    @Test
    public void testUpdatePlaylistInternalServerError() {
        webClient.get()
                .uri(String.format("/api/v1/updateplaylist?itemUris=%s", "spotify:track:123456789"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        verify(spotifyApiService, times(1)).updatePlaylist(anyString());
    }

}
