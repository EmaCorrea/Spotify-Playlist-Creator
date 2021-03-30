package com.emacorrea.spc.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class SpotifyApiConfig {

    @Value("${spotify.api.clientId}")
    private String clientId;

    @Value("${spotify.api.clientSecret}")
    private String clientSecret;

    @Value("${spotify.api.refreshToken}")
    private String refreshToken;

    @Value("${spotify.api.playlistId}")
    private String playlistId;

    public void test() {
        String test = "test";
    }
}
