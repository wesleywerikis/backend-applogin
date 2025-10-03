package br.com.applogin.backend_applogin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.applogin.backend_applogin.dto.UserResponse;
import br.com.applogin.backend_applogin.mapper.UserMapper;
import br.com.applogin.backend_applogin.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService users;
    private final UserMapper mapper;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication auth) {
        var u = users.findByUsernameOrEmail(auth.getName()).orElseThrow();
        return ResponseEntity.ok(mapper.toResponse(u));
    }

}
