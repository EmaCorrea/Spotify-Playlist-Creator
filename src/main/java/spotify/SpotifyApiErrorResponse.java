package spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyApiErrorResponse {

//    private Map<String, Error> error;
//    private List<Error> error;
    private Error error;

    @Data
    public static class Error {
        private int status;
        private String message;
    }
}
