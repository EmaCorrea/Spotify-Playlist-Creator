package com.emacorrea.spc.rest;

import com.emacorrea.spc.AppConstants;
import com.emacorrea.spc.service.SpotifyApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import model.TopTracksResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

// TODO: Create response handler
@Api(tags = "Spotify Playlist Creator API")
@RestController
@RequestMapping(path = "/api/v1/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
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
            @ApiResponse(code = 404, message = AppConstants.MSG_NOT_FOUND),
            @ApiResponse(code = 406, message = AppConstants.MSG_NOT_ACCEPTABLE)
    })
    @GetMapping("/toptracks")
    public Mono<TopTracksResponse> getTopTracks() {
        return spotifyApiService.getUsersTopTracks();
    }

    // TODO: Change to post
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
            @ApiResponse(code = 404, message = AppConstants.MSG_NOT_FOUND),
            @ApiResponse(code = 406, message = AppConstants.MSG_NOT_ACCEPTABLE)
    })
    @GetMapping("/updateplaylist/{itemUris}")
    public Mono<String> updatePlaylist(
            @Valid @PathVariable String itemUris) {
        return spotifyApiService.replacePlaylistItems(itemUris);
    }

    // TODO: Add an endpoint to retrieve the current weekly top tracks playlist

}
