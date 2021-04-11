package spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyTopTracksResponse {

    private Item[] items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        public Artist[] artists;
        public String name;
        public String uri;

        public void test() {
            System.out.println("");
        }
    }

    @Data
    public static class Artist {
        public String name;
    }

}
