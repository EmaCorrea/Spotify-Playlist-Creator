package com.emacorrea.spc.batch.updateplaylist;

import com.emacorrea.spc.service.SpotifyApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import spotify.SpotifyTopTracksResponse;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class UpdatePlaylistTasklet implements Tasklet {

    private SpotifyApiService spotifyApiService;

    @Autowired
    public UpdatePlaylistTasklet(@NotNull SpotifyApiService spotifyApiService) {
        this.spotifyApiService = spotifyApiService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Mono<SpotifyTopTracksResponse> topTracksResponse = spotifyApiService.getUsersTopTracks();

        topTracksResponse
                .doOnSuccess(responseTopTracks -> {
                    log.info("Successfully retrieved user's top tracks: {}", responseTopTracks.toString());

                    String test = Arrays.stream(responseTopTracks.getItems())
                            .map(item -> item.getTrackUri())
                            .collect(Collectors.joining(","));

                    spotifyApiService.updatePlaylist(test)
                            .doOnSuccess(responseUpdate -> log.info("Successfully replaced playlist items: {}", responseUpdate.getSnapshotId()))
                            .doOnError(error -> log.error("Error replacing playlist items: {}", error.getMessage()))
                            .subscribe();
                })
                .doOnError(error -> log.error("Error retrieving user's top tracks: {}", error.getMessage()))
                .subscribe();

        return RepeatStatus.FINISHED;
    }

}
