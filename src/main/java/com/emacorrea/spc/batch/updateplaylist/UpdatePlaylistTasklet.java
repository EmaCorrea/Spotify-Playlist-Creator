package com.emacorrea.spc.batch.updateplaylist;

import com.emacorrea.spc.service.SpotifyApiService;
import com.emacorrea.spc.service.SpotifyApiService2;
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
    private SpotifyApiService2 spotifyApiService2;

    @Autowired
    public UpdatePlaylistTasklet(@NotNull SpotifyApiService spotifyApiService,
                                 @NotNull SpotifyApiService2 spotifyApiService2) {
        this.spotifyApiService = spotifyApiService;
        this.spotifyApiService2 = spotifyApiService2;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
//        Mono<String> topTracksResponse = spotifyApiService.getUsersTopTracksUris();
//
//        topTracksResponse
//                .doOnNext(itemUris -> spotifyApiService.replacePlaylistItems(itemUris))
//                .doOnSuccess(r -> log.info("Successfully replaced playlist items"))
//                .doOnError(e -> log.error("Error replacing playlist items: {}", e.getMessage()))
//                .subscribe();
//
//        return RepeatStatus.FINISHED;

//        if(trackPaging != null) {
//            return Arrays.stream(trackPaging.getItems())
//                    .map(track ->   track.getUri())
//                    .collect(Collectors.joining(","));

        Mono<SpotifyTopTracksResponse> topTracksResponse = spotifyApiService2.getusersTopTracks();

        topTracksResponse
                .doOnSuccess(t -> {
                    String test = Arrays.stream(t.getItems())
                            .map(r -> r.getUri())
                            .collect(Collectors.joining(","));
                    spotifyApiService2.updatePlaylist(test)
                            .doOnSuccess(s -> log.info("Successfully replaced playlist items: {}", s.getSnapshot_id()))
                            .doOnError(e -> log.error("Error replacing playlist items: {}", e.getMessage()))
                            .subscribe();
                })
                .subscribe();

        return RepeatStatus.FINISHED;
    }

}
