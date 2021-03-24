package com.emacorrea.spc.batch.updateplaylist;

import com.emacorrea.spc.service.SpotifyApiService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class UpdatePlaylistTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        SpotifyApiService spotifyApiService = new SpotifyApiService();

        spotifyApiService.getUsersTopTracks_Sync();

        return null;
    }

}
