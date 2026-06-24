package com.netflix.identity_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private long expiresIn;
    private String accessToken;
    private String tokenType = "Bearer"; // Standards compliance


    public AuthResponse(String accessToken, long jwtExpirationTimeMs) {
        this.accessToken = accessToken;
        this.expiresIn = jwtExpirationTimeMs;
    }
}