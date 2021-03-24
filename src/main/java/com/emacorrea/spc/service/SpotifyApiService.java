package com.emacorrea.spc.service;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;
import com.wrapper.spotify.requests.data.playlists.ReplacePlaylistsItemsRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
@Slf4j
public class SpotifyApiService {

    private static final String clientId = "";
    private static final String clientSecret = "";
    private static final String refreshToken = "";

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRefreshToken(refreshToken)
            .build();

    private static final AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
            .build();

    private static AuthorizationCodeCredentials authorizationCodeCredentials;

    public static void authorizationCodeRefresh_Sync() {
        try {
            authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());

            log.info("Expires in: " + authorizationCodeCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    public Mono<Track[]> getUsersTopTracks_Sync() {
        try {
            authorizationCodeRefresh_Sync();
            GetUsersTopTracksRequest getUsersTopTracksRequest = spotifyApi.getUsersTopTracks()
                    .limit(20)
                    .time_range("short_term")
                    .build();

            final Paging<Track> trackPaging = getUsersTopTracksRequest.execute();

            StringBuilder sb = new StringBuilder();

            // Displays tracks
            for(Track t : trackPaging.getItems()) {
                log.info(t.getArtists()[0].getName() + " - " + t.getName());
                sb.append("spotify:track:" + t.getId() + ",");
            }

            final String playlistId = "4kv5RSV9UaK9zdO6c5rlrZ";
            final String[] uris = new String[]{sb.toString()};

            ReplacePlaylistsItemsRequest replacePlaylistsItemsRequest = spotifyApi
                    .replacePlaylistsItems(playlistId, uris)
                    .build();

            String string = replacePlaylistsItemsRequest.execute();

            log.info("Null: " + string);

            return Mono.just(trackPaging.getItems())
                    .doOnNext(track -> log.info("Total: " + trackPaging.getTotal()));

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Error: " + e.getMessage());
        }
        return null;
    }

}
