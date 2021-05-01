package com.emacorrea.spc.rest;

import com.emacorrea.spc.AppConstants;
import com.emacorrea.spc.service.SpotifyApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import spotify.SpotifyTopTracksResponse;
import spotify.SpotifyUpdatePlaylistResponse;

import javax.validation.Valid;

// TODO: Create error response handler
@Api(tags = "Spotify Playlist Creator API")
@RestController
@RequestMapping(path = "/api/v1/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
@Slf4j
public class Controller {

    private final SpotifyApiService spotifyApiService;

    /**
     * Returns a list of the user's top tracks in Spotify
     * @return top tracks
     */
    @ApiOperation(value = "Get list of the user's top tracks from approximately the last 4 weeks",
            httpMethod = AppConstants.HTTP_METHOD_GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = AppConstants.MSG_SUCCESS),
            @ApiResponse(code = 400, message = AppConstants.MSG_BAD_REQUEST),
            @ApiResponse(code = 401, message = AppConstants.MSG_UNAUTHORIZED),
            @ApiResponse(code = 403, message = AppConstants.MSG_FORBIDDEN),
            @ApiResponse(code = 404, message = AppConstants.MSG_NOT_FOUND),
            @ApiResponse(code = 429, message = AppConstants.MSG_TOO_MANY_REQUESTS)
    })
    @GetMapping("/toptracks")
    public Mono<SpotifyTopTracksResponse> getTopTracks() {
        return spotifyApiService.getUsersTopTracks()
                .doOnSuccess(response -> log.info("Successfully retrieved user's top tracks: {}", response.toString()))
                .doOnError(error -> log.error("Error retrieving user's top tracks: {}", error.getMessage()));
    }

    /**
     * Returns confirmation that a Spotify playlist's items were replaces/updated
     * @return playlist replacement confirmation
     */
    @ApiOperation(value = "Updates any of my existing playlists",
            httpMethod = AppConstants.HTTP_METHOD_GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = AppConstants.MSG_SUCCESS),
            @ApiResponse(code = 400, message = AppConstants.MSG_BAD_REQUEST),
            @ApiResponse(code = 401, message = AppConstants.MSG_UNAUTHORIZED),
            @ApiResponse(code = 403, message = AppConstants.MSG_FORBIDDEN),
            @ApiResponse(code = 404, message = AppConstants.MSG_NOT_FOUND),
            @ApiResponse(code = 429, message = AppConstants.MSG_TOO_MANY_REQUESTS)
    })
    @GetMapping("/updateplaylist")
    public Mono<SpotifyUpdatePlaylistResponse> updatePlaylist(
            @Valid @RequestParam final String itemUris) {
        return spotifyApiService.updatePlaylist(itemUris)
                .doOnSuccess(response -> log.info("Successfully replaced playlist items: {}", response.getSnapshotId()))
                .doOnError(error -> log.error("Error replacing playlist items: {}", error.getMessage()));
    }

    // TODO: Add an endpoint to retrieve the current weekly top tracks playlist

}
