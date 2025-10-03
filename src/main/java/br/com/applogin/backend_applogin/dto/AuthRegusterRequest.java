package br.com.applogin.backend_applogin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegusterRequest(

        @NotBlank @Size(min = 3, max = 100) String username,

        @NotBlank @Email String email,

        @NotBlank @Size(min = 6, max = 72) String password) {

}
