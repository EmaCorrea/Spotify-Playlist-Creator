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

    private SpotifyApiService spotifyApiService2;

    @Autowired
    public UpdatePlaylistTasklet(@NotNull SpotifyApiService spotifyApiService2) {
        this.spotifyApiService2 = spotifyApiService2;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Mono<SpotifyTopTracksResponse> topTracksResponse = spotifyApiService2.getUsersTopTracks();

        topTracksResponse
                .doOnSuccess(t -> {
                    String test = Arrays.stream(t.getItems())
                            .map(r -> r.getTrackUri())
                            .collect(Collectors.joining(","));
                    spotifyApiService2.updatePlaylist(test)
                            .doOnSuccess(s -> log.info("Successfully replaced playlist items: {}", s.getSnapshotId()))
                            .doOnError(e -> log.error("Error replacing playlist items: {}", e.getMessage()))
                            .subscribe();
                })
                .subscribe();

        return RepeatStatus.FINISHED;
    }

}
