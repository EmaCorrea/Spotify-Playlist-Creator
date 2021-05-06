package com.emacorrea.spc.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyPlaylistTracksResponse {

    private Item[] items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        public Track track;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Track {
        public String name;
        private Artist[] artists;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Artist {
        @JsonProperty("name")
        public String artistName;
    }

    @Override
    public String toString() {
        final AtomicInteger counter = new AtomicInteger(0);
        final AtomicReference<String> playlistString = new AtomicReference<>("\nPlaylist tracks:\n");
        final AtomicReference<String> trackString = new AtomicReference<>("");
        final AtomicReference<String> artistString = new AtomicReference<>("");

        Arrays.stream(items).forEach(track -> {
            trackString.set(track.getTrack().getName());
            Arrays.stream(track.getTrack().getArtists()).forEach(artist -> {
                if(artistString.get().isEmpty()) {
                    artistString.set(artist.artistName);
                } else {
                    artistString.set(artist.artistName + ", " + artistString.get());
                }
            });
            counter.getAndIncrement();
            playlistString.set(playlistString.get() + counter + ". " + trackString.get() + " - " + artistString.get() + "\n");
            artistString.set("");
        });

        return playlistString.toString();
    }

}
