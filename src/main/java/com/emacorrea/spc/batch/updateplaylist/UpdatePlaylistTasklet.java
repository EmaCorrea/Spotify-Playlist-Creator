package com.emacorrea.spc.batch.updateplaylist;

import com.emacorrea.spc.service.SpotifyApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;

@Slf4j
public class UpdatePlaylistTasklet implements Tasklet {

    private SpotifyApiService spotifyApiService;

    @Autowired
    public UpdatePlaylistTasklet(@NotNull SpotifyApiService spotifyApiService) {
        this.spotifyApiService = spotifyApiService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Mono<String> topTracksResponse = spotifyApiService.getUsersTopTracksUris();

        topTracksResponse
                .doOnNext(itemUris -> spotifyApiService.replacePlaylistItems(itemUris))
                .doOnSuccess(r -> log.info("Successfully replaced playlist items"))
                .doOnError(e -> log.error("Error replacing playlist items: {}", e.getMessage()))
                .subscribe();

        return RepeatStatus.FINISHED;
    }

}
