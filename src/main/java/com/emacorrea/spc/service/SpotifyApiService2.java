package com.emacorrea.spc.service;

import com.emacorrea.spc.config.SpotifyApiConfig;
import com.wrapper.spotify.exceptions.detailed.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import spotify.SpotifyApiErrorResponse;
import spotify.SpotifyAuthResponse;
import spotify.SpotifyTopTracksResponse;
import spotify.SpotifyUpdatePlaylistResponse;

import java.time.Duration;

@Service
@Slf4j
public class SpotifyApiService2 {

    private final SpotifyApiConfig spotifyApiConfig;
    private final WebClient.Builder builder;
    private final WebClient authorizationClient;

    private WebClient client;

    public SpotifyApiService2(SpotifyApiConfig spotifyApiConfig, WebClient.Builder builder) {
        this.spotifyApiConfig = spotifyApiConfig;
        this.builder = builder;

        authorizationClient = builder.clone()
                .baseUrl(spotifyApiConfig.getAuthUri())
                .defaultHeaders(header -> {
                    header.setBasicAuth(spotifyApiConfig.getClientId(), spotifyApiConfig.getClientSecret());
                    header.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
                })
                .build();

        authorizationCodeRefresh();
    }

    public Mono<SpotifyTopTracksResponse> getusersTopTracks() {
        authorizationCodeRefresh();
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/me/top/tracks")
                        .queryParam("time_range", "short_term")
                        .queryParam("limit", "20")
                        .build())
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, SpotifyApiService2::mapError)
                .bodyToMono(SpotifyTopTracksResponse.class)
                .retryWhen(retrySpec());
    }

    public Mono<SpotifyUpdatePlaylistResponse> updatePlaylist(String uris) {
        authorizationCodeRefresh();

        return client.put()
                .uri(uriBuilder -> uriBuilder
                        .path(String.format("/v1/playlists/%s/tracks", spotifyApiConfig.getPlaylistId()))
                        .queryParam("uris", uris)
//                        .queryParam("limit", "20")
                        .build())
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, SpotifyApiService2::mapError)
                .bodyToMono(SpotifyUpdatePlaylistResponse.class)
                .retryWhen(retrySpec());
    }

    private void authorizationCodeRefresh() {
        MultiValueMap bodyMap = new LinkedMultiValueMap();
        bodyMap.add("grant_type", "refresh_token");
        bodyMap.add("refresh_token", spotifyApiConfig.getRefreshToken());

        Mono<SpotifyAuthResponse> response = authorizationClient.post()
                .uri("/api/token")
                .body(BodyInserters.fromFormData(bodyMap))
                .retrieve()
                .bodyToMono(SpotifyAuthResponse.class);

        response.subscribe(t -> {
            client = builder.clone()
                    .baseUrl(spotifyApiConfig.getBaseUri())
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + t.getAccess_token())
                    .build();
        });
    }

    private static Mono<? extends Throwable> mapError(final ClientResponse response) {
        return response.bodyToMono(SpotifyApiErrorResponse.class)
                .doOnNext(err -> log.error("Error calling Spotify: {}", err.getError().get(0).getMessage()))
                .flatMap(err -> {
                    final String errorMsg = err.getError().get(0).getMessage();
                    switch (err.getError().get(0).getStatus()) {
                        case 400: return Mono.error(new BadRequestException(errorMsg));
                        case 401: return Mono.error(new UnauthorizedException(errorMsg));
                        case 403: return Mono.error(new ForbiddenException(errorMsg));
                        case 404: return Mono.error(new NotFoundException(errorMsg));
                        case 429: return Mono.error(new TooManyRequestsException(errorMsg));
                        default: return Mono.error(new InternalServerErrorException(errorMsg));
                    }
                }
                );
    }

    private static Retry retrySpec() {
        return Retry.fixedDelay(3, Duration.ofSeconds(1))
                .filter(throwable -> throwable instanceof WebClientResponseException &&
                        ((WebClientResponseException) throwable).getStatusCode().is5xxServerError());
    }
}
