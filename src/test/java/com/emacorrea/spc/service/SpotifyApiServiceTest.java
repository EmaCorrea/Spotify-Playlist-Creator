package com.emacorrea.spc.service;

import com.emacorrea.spc.config.SpotifyApiConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import spotify.SpotifyTopTracksResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
//        SpotifyApiService.class,
        SpotifyApiConfig.class
})
@AutoConfigureWebClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SpotifyApiServiceTest {

    private MockWebServer oandaServer;
    private ObjectMapper objectMapper;

    private SpotifyApiService spotifyApiService;

    @MockBean
    private SpotifyApiConfig spotifyApiConfig;


    @BeforeEach
    public void beforeEach() throws IOException {
        oandaServer = new MockWebServer();
        oandaServer.start();
        objectMapper = new ObjectMapper();
        when(spotifyApiConfig.getClientId()).thenReturn("clientId");
        when(spotifyApiConfig.getClientSecret()).thenReturn("clientSecret");
        when(spotifyApiConfig.getRefreshToken()).thenReturn("refreshToken");
        when(spotifyApiConfig.getPlaylistId()).thenReturn("playlistId");
        when(spotifyApiConfig.getBaseUri()).thenReturn("http://localhost:" + oandaServer.getPort());
        when(spotifyApiConfig.getAuthUri()).thenReturn("http://localhost");
        spotifyApiService = new SpotifyApiService(spotifyApiConfig, WebClient.builder());
    }

    @AfterEach
    public void afterEach() throws IOException {
        oandaServer.shutdown();
    }

    @Test
    public void testGetUsersTopTracks() throws JsonProcessingException {
//        final SpotifyTopTracksResponse.Item items = new SpotifyTopTracksResponse.Item[];
//        final SpotifyTopTracksResponse.Item[] items = new SpotifyTopTracksResponse.Item[];

        final SpotifyTopTracksResponse spotifyTopTracksResponse = SpotifyTopTracksResponse.builder()
                .items(new SpotifyTopTracksResponse.Item[]{
                        new SpotifyTopTracksResponse.Item(new SpotifyTopTracksResponse.Artist[]{
                                new SpotifyTopTracksResponse.Artist("testArtistName")
                        }, "testTrackName", "testTrackUri")
                })
                .build();

        final SpotifyTopTracksResponse expectedSpotifyTopTracksResponse =SpotifyTopTracksResponse.builder()
                .items(new SpotifyTopTracksResponse.Item[]{
                        new SpotifyTopTracksResponse.Item(new SpotifyTopTracksResponse.Artist[]{
                                new SpotifyTopTracksResponse.Artist("testArtistName")
                        }, "testTrackName", "testTrackUri")
                })
                .build();

        oandaServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(spotifyTopTracksResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        final Mono<SpotifyTopTracksResponse> actualExchangeRate = spotifyApiService.getUsersTopTracks();
        StepVerifier.create(actualExchangeRate)
                .expectNext(expectedSpotifyTopTracksResponse)
                .expectComplete()
                .verify();

        Assertions.assertEquals(1, oandaServer.getRequestCount());
    }
}
