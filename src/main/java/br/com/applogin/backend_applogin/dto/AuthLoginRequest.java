package br.com.applogin.backend_applogin.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(

        @NotBlank String usernameOrEmail,
        @NotBlank String password

) {

}
