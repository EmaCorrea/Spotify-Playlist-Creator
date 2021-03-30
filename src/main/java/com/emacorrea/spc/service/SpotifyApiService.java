package com.emacorrea.spc.service;

import com.emacorrea.spc.config.SpotifyApiConfig;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;
import com.wrapper.spotify.requests.data.playlists.ReplacePlaylistsItemsRequest;
import lombok.extern.slf4j.Slf4j;
import model.TopTracksResponse;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SpotifyApiService {

    private final SpotifyApiConfig spotifyApiConfig;

    private SpotifyApi spotifyApi;
    private AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest;
    private AuthorizationCodeCredentials authorizationCodeCredentials;

    @Autowired
    public SpotifyApiService(SpotifyApiConfig spotifyApiConfig) {
        this.spotifyApiConfig = spotifyApiConfig;
    }

    public Mono<TopTracksResponse> getUsersTopTracks() {
        try {
            authorizationCodeRefresh();

            GetUsersTopTracksRequest getUsersTopTracksRequest = spotifyApi.getUsersTopTracks()
                    .limit(20)
                    .time_range("short_term")
                    .build();

            Paging<Track> trackPaging = getUsersTopTracksRequest.execute();

            if(trackPaging != null) {
                TopTracksResponse tracks = requestUsersTopTracksNames(trackPaging);

                return Mono.just(tracks)
                        .doOnNext(r -> {
                            log.info("Successfully retrieved top tracks");
                            if("".equals(tracks)) {
                                log.info("No top tracks available");
                            }
                        })
                        .doOnError(e -> log.error("Error retrieving top tracks: {}", e.getMessage()));
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Error replacing playlist items: {}", e.getMessage());
        }
        return Mono.empty();
    }

    public Mono<String> getUsersTopTracksUris() {
        authorizationCodeRefresh();

        final String itemUris = requestUsersTopTracks();

        return Mono.just(itemUris)
                .doOnNext(t -> {
                    log.info("Successfully retrieved top tracks");
                    if("".equals(t)) {
                        log.info("No top tracks available");
                    }
                })
                .doOnError(e -> log.error("Error retrieving top tracks: {}", e.getMessage()));
    }

    public Mono<String> replacePlaylistItems(String itemUris) {
        try {
            authorizationCodeRefresh();

            ReplacePlaylistsItemsRequest replacePlaylistsItemsRequest = spotifyApi
                    .replacePlaylistsItems(spotifyApiConfig.getPlaylistId(), itemUris.split(","))
                    .build();

            String response = replacePlaylistsItemsRequest.execute();

            return Mono.just(response)
                    .doOnNext(r -> {
                        if("".equals(response)) {
                            log.info("Playlist could not be updated");
                        } else {
                            log.info("Successfully replaced playlist items");
                        }
                    })
                    .doOnError(e -> log.error("Error replacing playlist items: {}", e.getMessage()));
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Error replacing playlist items: {}", e.getMessage());
        }
        return Mono.empty();
    }

    private void authorizationCodeRefresh() {
        try {
            spotifyApi = new SpotifyApi.Builder()
                    .setClientId(spotifyApiConfig.getClientId())
                    .setClientSecret(spotifyApiConfig.getClientSecret())
                    .setRefreshToken(spotifyApiConfig.getRefreshToken())
                    .build();

            authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
                    .build();

            authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());

            log.info("Expires in: {}ms", authorizationCodeCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Error requesting authorization code credentials: {}", e.getMessage());
        }
    }

    private String requestUsersTopTracks() {
        try {
            GetUsersTopTracksRequest getUsersTopTracksRequest = spotifyApi.getUsersTopTracks()
                    .limit(20)
                    .time_range("short_term")
                    .build();

            Paging<Track> trackPaging = getUsersTopTracksRequest.execute();

            if(trackPaging != null) {
                return Arrays.stream(trackPaging.getItems())
                        .map(track ->   track.getUri())
                        .collect(Collectors.joining(","));
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Error requesting user's top tracks: " + e.getMessage());
        }
        return "";
    }

    private TopTracksResponse requestUsersTopTracksNames(Paging<Track> trackPaging) {
        AtomicInteger counter = new AtomicInteger(0);

        // Logging top tracks
        String trackNames = String.format("Top Tracks: %s", Arrays.stream(trackPaging.getItems())
                .map(track -> {
                    counter.getAndIncrement();
                    return String.format("\n%d. %s - %s", counter.get(), track.getArtists()[0].getName(), track.getName());
                })
                .collect(Collectors.joining(" | "))
        );

        log.info(trackNames);

        Map<String, TopTracksResponse.Track> topTracksMap = new HashMap<>();

        // Creating top tracks response
        Arrays.stream(trackPaging.getItems()).forEach(track -> {
            if(topTracksMap.containsKey(track.getArtists()[0].getName())) {
                topTracksMap.get(track.getArtists()[0].getName()).getTracks().add(track.getName());
            } else {
                topTracksMap.put(
                        track.getArtists()[0].getName(),
                        new TopTracksResponse.Track(new ArrayList<>(Arrays.asList(track.getName())))
                );
            }
        });

        return TopTracksResponse.builder()
                .artists(topTracksMap)
                .build();
    }

}
