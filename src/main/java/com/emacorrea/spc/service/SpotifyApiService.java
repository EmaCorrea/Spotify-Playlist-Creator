package com.emacorrea.spc.service;

import com.emacorrea.spc.config.SpotifyApiConfig;
import com.emacorrea.spc.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import spotify.SpotifyApiErrorResponse;
import spotify.SpotifyAuthResponse;
import spotify.SpotifyTopTracksResponse;
import spotify.SpotifyUpdatePlaylistResponse;

import java.time.Duration;
@Slf4j
@Service
public class SpotifyApiService {

    private SpotifyApiConfig spotifyApiConfig;
    private WebClient.Builder builder;
    private WebClient authorizationClient;

    private WebClient spotifyClient;

    public SpotifyApiService(SpotifyApiConfig spotifyApiConfig, WebClient.Builder builder) {
        this.spotifyApiConfig = spotifyApiConfig;
        this.builder = builder;

        authorizationClient = builder.clone()
                .baseUrl(spotifyApiConfig.getAuthUri())
                .defaultHeaders(header -> {
                    header.setBasicAuth(spotifyApiConfig.getClientId(), spotifyApiConfig.getClientSecret());
                    header.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
                })
                .build();
    }

    public Mono<SpotifyTopTracksResponse> getUsersTopTracks() {
        authorizationCodeRefresh();
        return spotifyClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/me/top/tracks")
                        .queryParam("time_range", "short_term")
                        .queryParam("limit", "20")
                        .build())
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, SpotifyApiService::mapError)
                .bodyToMono(SpotifyTopTracksResponse.class)
                .retryWhen(retrySpec());
    }

    public Mono<SpotifyUpdatePlaylistResponse> updatePlaylist(String uris) {
        authorizationCodeRefresh();
        return spotifyClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(String.format("/v1/playlists/%s/tracks", spotifyApiConfig.getPlaylistId()))
                        .queryParam("uris", uris)
//                        .queryParam("limit", "20")
                        .build())
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, SpotifyApiService::mapError)
                .bodyToMono(SpotifyUpdatePlaylistResponse.class)
                .retryWhen(retrySpec());
    }

    private void authorizationCodeRefresh() {
        RestTemplate restTemplate = new RestTemplate();

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(spotifyApiConfig.getScheme())
                .host(spotifyApiConfig.getAuthUri())
                .path("/api/token")
                .queryParam("time_range", "short_term")
                .queryParam("limit", "20")
                .build();

        MultiValueMap bodyMap = new LinkedMultiValueMap();
        bodyMap.add("grant_type", "refresh_token");
        bodyMap.add("refresh_token", spotifyApiConfig.getRefreshToken());

        ResponseEntity<SpotifyAuthResponse> responseEntity = restTemplate.exchange(uriComponents.toUriString(),
                HttpMethod.POST,
                new HttpEntity<>(bodyMap, getSpotifyAuthClientHeaders()),
                SpotifyAuthResponse.class);

        spotifyClient = builder.clone()
                .baseUrl(spotifyApiConfig.getBaseUri())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + responseEntity.getBody().getAccessToken())
                .build();
    }

    private HttpHeaders getSpotifyAuthClientHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(spotifyApiConfig.getClientId(), spotifyApiConfig.getClientSecret());
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
        return headers;
    }

    private static Mono<? extends Throwable> mapError(final ClientResponse response) {
        return response.bodyToMono(SpotifyApiErrorResponse.class)
                .doOnNext(err -> log.error("Error calling Spotify: {}", err.getError().getMessage()))
                .flatMap(error -> {
                    final String errorMsg = error.getError().getMessage();
                    switch (error.getError().getStatus()) {
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
