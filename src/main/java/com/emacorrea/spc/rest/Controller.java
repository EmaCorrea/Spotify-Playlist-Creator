package com.emacorrea.spc.rest;

import com.emacorrea.spc.AppConstants;
import com.emacorrea.spc.service.SpotifyApiService;
import com.wrapper.spotify.model_objects.specification.Track;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Api(tags = "Spotify Playlist Creator API")
@RestController
@RequestMapping(path = "/api/v1/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
public class Controller {

    private final SpotifyApiService spotifyApiService;

    /**
     * Return list of the user's top tracks in Spotify
     * @return top tracks
     */
    @ApiOperation(value = "Get list of currencies",
            httpMethod = AppConstants.HTTP_METHOD_GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = AppConstants.MSG_SUCCESS),
            @ApiResponse(code = 400, message = AppConstants.MSG_BAD_REQUEST),
            @ApiResponse(code = 401, message = AppConstants.MSG_UNAUTHORIZED),
            @ApiResponse(code = 403, message = AppConstants.MSG_FORBIDDEN),
            @ApiResponse(code = 404, message = AppConstants.MSG_NOT_FOUND),
            @ApiResponse(code = 406, message = AppConstants.MSG_NOT_ACCEPTABLE),
            @ApiResponse(code = 500, message = AppConstants.MSG_INTERNAL_SERVER_ERROR)
    })
    @GetMapping("/toptracks")
    public Mono<Track[]> getTopTracks() {
        return spotifyApiService.getUsersTopTracks_Sync();
    }

}
