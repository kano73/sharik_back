package com.mary.sharik.model.dto.responce;

import lombok.Data;

@Data
public class TokenPair {
    private String accessToken;
    private String refreshToken;

    public TokenPair(String newAccessToken, String newRefreshToken) {
        accessToken = newAccessToken;
        refreshToken = newRefreshToken;
    }
}