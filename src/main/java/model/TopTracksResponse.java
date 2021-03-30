package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TopTracksResponse {

    @NotNull
    @ApiModelProperty(example = "Modest Mouse")
    private Map<String, Track> artists;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Track {

        @NotNull
        @ApiModelProperty(example = "Ocean Breathes Salty")
        private ArrayList<String> tracks;
    }
}
