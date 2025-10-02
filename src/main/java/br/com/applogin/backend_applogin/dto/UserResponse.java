package br.com.applogin.backend_applogin.dto;

import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        Boolean enabled,
        Instant createdAt,
        Set<String> roles) {

}
