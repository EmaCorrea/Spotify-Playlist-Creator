package com.emacorrea.spc.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyTopTracksResponse {

    private Item[] items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        public Artist[] artists;

        @JsonProperty("name")
        public String trackName;

        @JsonProperty("uri")
        public String trackUri;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Artist {
        @JsonProperty("name")
        public String artistName;
    }

}
