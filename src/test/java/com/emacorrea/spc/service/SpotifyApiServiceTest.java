package com.emacorrea.spc.service;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.util.stream.Stream;

import com.emacorrea.spc.config.SpotifyApiConfig;
import com.emacorrea.spc.spotify.SpotifyAuthResponse;
import com.emacorrea.spc.spotify.SpotifyTopTracksResponse;
import com.emacorrea.spc.spotify.SpotifyUpdatePlaylistResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = {
        SpotifyApiConfig.class
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class SpotifyApiServiceTest {

    private static final String TEST_ACCESS_TOKEN = "testAcessToken";
    private static final String TEST_TOKEN_TYPE = "testTokenType";
    private static final String TEST_TOKEN_SCOPE = "testScope";

    private MockWebServer spotifyServer;
    private ObjectMapper objectMapper;

    private SpotifyApiService spotifyApiService;

    @MockBean
    private SpotifyApiConfig spotifyApiConfig;

    @BeforeEach
    public void beforeEach() throws IOException {
        spotifyServer = new MockWebServer();
        spotifyServer.start();
        objectMapper = new ObjectMapper();
        when(spotifyApiConfig.getClientId()).thenReturn("clientId");
        when(spotifyApiConfig.getClientSecret()).thenReturn("clientSecret");
        when(spotifyApiConfig.getRefreshToken()).thenReturn("refreshToken");
        when(spotifyApiConfig.getPlaylistId()).thenReturn("playlistId");
        when(spotifyApiConfig.getBaseUri()).thenReturn("http://localhost:" + spotifyServer.getPort());
        when(spotifyApiConfig.getAuthUri()).thenReturn("localhost:" + spotifyServer.getPort());
        when(spotifyApiConfig.getScheme()).thenReturn("http");
        spotifyApiService = new SpotifyApiService(spotifyApiConfig, WebClient.builder());
    }

    @AfterEach
    public void afterEach() throws IOException {
        spotifyServer.shutdown();
    }

    @Test
    public void testGetUsersTopTracks() throws JsonProcessingException {
        final SpotifyAuthResponse spotifyAuthResponse = SpotifyAuthResponse.builder()
                .accessToken(TEST_ACCESS_TOKEN)
                .tokenType(TEST_TOKEN_TYPE)
                .expiresIn(1000)
                .scope(TEST_TOKEN_SCOPE)
                .build();

        final SpotifyTopTracksResponse expectedSpotifyTopTracksResponse = SpotifyTopTracksResponse.builder()
                .items(new SpotifyTopTracksResponse.Item[]{
                        new SpotifyTopTracksResponse.Item(new SpotifyTopTracksResponse.Artist[]{
                                new SpotifyTopTracksResponse.Artist("testArtistName")
                        }, "testTrackName", "testTrackUri")
                })
                .build();

        spotifyServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(spotifyAuthResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        spotifyServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expectedSpotifyTopTracksResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        final Mono<SpotifyTopTracksResponse> actualUsersTopTracksResponse = spotifyApiService.getUsersTopTracks();

        StepVerifier.create(actualUsersTopTracksResponse)
                .expectNext(expectedSpotifyTopTracksResponse)
                .expectComplete()
                .verify();

        assertEquals(2, spotifyServer.getRequestCount());
    }

    @ParameterizedTest(name = "{displayName} #{index}: {arguments}")
    @MethodSource("testGetUsersTopTracksErrorArgsProvider")
    public void testGetUsersTopTracksError(String errorBody, int errorCode,
                                           String errorException, String expectedErrorMsg) throws JsonProcessingException {
        final SpotifyAuthResponse spotifyAuthResponse = SpotifyAuthResponse.builder()
                .accessToken(TEST_ACCESS_TOKEN)
                .tokenType(TEST_TOKEN_TYPE)
                .expiresIn(1000)
                .scope(TEST_TOKEN_SCOPE)
                .build();

        spotifyServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(spotifyAuthResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        spotifyServer.enqueue(new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(errorBody)
                .setResponseCode(errorCode));

        final Mono<SpotifyTopTracksResponse> actualUsersTopTracksResponse = spotifyApiService.getUsersTopTracks();

        StepVerifier.create(actualUsersTopTracksResponse)
                .expectErrorMatches(throwable -> throwable.getClass().getSimpleName().equals(errorException) &&
                        throwable.getMessage().equals(expectedErrorMsg))
                .verify();

        assertEquals(2, spotifyServer.getRequestCount());
    }

    private static Stream<Arguments> testGetUsersTopTracksErrorArgsProvider() {
        return Stream.of(
                Arguments.arguments("{\"error\":{\"status\":400,\"message\":\"Bad Request\"}}", 400,
                        "BadRequestException", "Bad Request"),
                Arguments.arguments("{\"error\":{\"status\":401,\"message\":\"Unauthorized\"}}", 401,
                        "UnauthorizedException", "Unauthorized"),
                Arguments.arguments("{\"error\":{\"status\":403,\"message\":\"Forbidden\"}}", 403,
                        "ForbiddenException", "Forbidden"),
                Arguments.arguments("{\"error\":{\"status\":404,\"message\":\"Not Found\"}}", 403,
                        "NotFoundException", "Not Found"),
                Arguments.arguments("{\"error\":{\"status\":429,\"message\":\"Too Many Requests\"}}", 429,
                        "TooManyRequestsException", "Too Many Requests"),
                Arguments.arguments("{\"error\":{\"status\":405,\"message\":\"Method Not Allowed\"}}", 405,
                        "InternalServerErrorException", "Method Not Allowed")
        );
    }

    @Test
    public void testGetUsersTopTracksRetry() throws JsonProcessingException {
        final SpotifyAuthResponse spotifyAuthResponse = SpotifyAuthResponse.builder()
                .accessToken(TEST_ACCESS_TOKEN)
                .tokenType(TEST_TOKEN_TYPE)
                .expiresIn(1000)
                .scope(TEST_TOKEN_SCOPE)
                .build();

        final SpotifyTopTracksResponse expectedSpotifyTopTracksResponse = SpotifyTopTracksResponse.builder()
                .items(new SpotifyTopTracksResponse.Item[]{
                        new SpotifyTopTracksResponse.Item(new SpotifyTopTracksResponse.Artist[]{
                                new SpotifyTopTracksResponse.Artist("testArtistName")
                        }, "testTrackName", "testTrackUri")
                })
                .build();

        spotifyServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(spotifyAuthResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        spotifyServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expectedSpotifyTopTracksResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        spotifyServer.enqueue(new MockResponse().setResponseCode(500));

        StepVerifier.withVirtualTime(() -> spotifyApiService.getUsersTopTracks())
                .thenAwait(Duration.ofSeconds(2))
                .expectNext(expectedSpotifyTopTracksResponse)
                .expectComplete()
                .verify();

        assertEquals(2, spotifyServer.getRequestCount());
    }

    @Test
    public void testUpdatePlaylist() throws JsonProcessingException {
        final SpotifyAuthResponse spotifyAuthResponse = SpotifyAuthResponse.builder()
                .accessToken(TEST_ACCESS_TOKEN)
                .tokenType(TEST_TOKEN_TYPE)
                .expiresIn(1000)
                .scope(TEST_TOKEN_SCOPE)
                .build();

        final SpotifyUpdatePlaylistResponse expectedSpotifyUpdatePlaylistResponse = SpotifyUpdatePlaylistResponse.builder()
                .snapshotId("idTest")
                .build();

        spotifyServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(spotifyAuthResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        spotifyServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expectedSpotifyUpdatePlaylistResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        final Mono<SpotifyUpdatePlaylistResponse> actualUpdatePlaylistResponse =
                spotifyApiService.updatePlaylist("spotify:track:123456789");

        StepVerifier.create(actualUpdatePlaylistResponse)
                .expectNext(expectedSpotifyUpdatePlaylistResponse)
                .expectComplete()
                .verify();

        assertEquals(2, spotifyServer.getRequestCount());
    }

    @ParameterizedTest(name = "{displayName} #{index}: {arguments}")
    @MethodSource("testUpdatePlaylistErrorArgsProvider")
    public void testUpdatePlaylistError(String errorBody, int errorCode,
                                           String errorException, String expectedErrorMsg) throws JsonProcessingException {
        final SpotifyAuthResponse spotifyAuthResponse = SpotifyAuthResponse.builder()
                .accessToken(TEST_ACCESS_TOKEN)
                .tokenType(TEST_TOKEN_TYPE)
                .expiresIn(1000)
                .scope(TEST_TOKEN_SCOPE)
                .build();

        spotifyServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(spotifyAuthResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        spotifyServer.enqueue(new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(errorBody)
                .setResponseCode(errorCode));

        final Mono<SpotifyUpdatePlaylistResponse> actualUpdatePlaylistResponse =
                spotifyApiService.updatePlaylist("spotify:track:123456789");

        StepVerifier.create(actualUpdatePlaylistResponse)
                .expectErrorMatches(throwable -> throwable.getClass().getSimpleName().equals(errorException) &&
                        throwable.getMessage().equals(expectedErrorMsg))
                .verify();

        assertEquals(2, spotifyServer.getRequestCount());
    }

    private static Stream<Arguments> testUpdatePlaylistErrorArgsProvider() {
        return Stream.of(
                Arguments.arguments("{\"error\":{\"status\":400,\"message\":\"Bad Request\"}}", 400,
                        "BadRequestException", "Bad Request"),
                Arguments.arguments("{\"error\":{\"status\":401,\"message\":\"Unauthorized\"}}", 401,
                        "UnauthorizedException", "Unauthorized"),
                Arguments.arguments("{\"error\":{\"status\":403,\"message\":\"Forbidden\"}}", 403,
                        "ForbiddenException", "Forbidden"),
                Arguments.arguments("{\"error\":{\"status\":404,\"message\":\"Not Found\"}}", 403,
                        "NotFoundException", "Not Found"),
                Arguments.arguments("{\"error\":{\"status\":429,\"message\":\"Too Many Requests\"}}", 429,
                        "TooManyRequestsException", "Too Many Requests"),
                Arguments.arguments("{\"error\":{\"status\":405,\"message\":\"Method Not Allowed\"}}", 405,
                        "InternalServerErrorException", "Method Not Allowed")
        );
    }

    @Test
    public void testUpdatePlaylistRetry() throws JsonProcessingException {
        final SpotifyAuthResponse spotifyAuthResponse = SpotifyAuthResponse.builder()
                .accessToken(TEST_ACCESS_TOKEN)
                .tokenType(TEST_TOKEN_TYPE)
                .expiresIn(1000)
                .scope(TEST_TOKEN_SCOPE)
                .build();

        final SpotifyUpdatePlaylistResponse expectedSpotifyUpdatePlaylistResponse = SpotifyUpdatePlaylistResponse.builder()
                .snapshotId("idTest")
                .build();

        spotifyServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(spotifyAuthResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        spotifyServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expectedSpotifyUpdatePlaylistResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        spotifyServer.enqueue(new MockResponse().setResponseCode(500));

        StepVerifier.withVirtualTime(() -> spotifyApiService.updatePlaylist("spotify:track:123456789"))
                .thenAwait(Duration.ofSeconds(2))
                .expectNext(expectedSpotifyUpdatePlaylistResponse)
                .expectComplete()
                .verify();

        assertEquals(2, spotifyServer.getRequestCount());
    }

}
