package br.com.applogin.backend_applogin.controller;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.applogin.backend_applogin.auth.JwtUtil;
import br.com.applogin.backend_applogin.domain.entity.User;
import br.com.applogin.backend_applogin.dto.AuthLoginRequest;
import br.com.applogin.backend_applogin.dto.AuthRegisterRequest;
import br.com.applogin.backend_applogin.mapper.UserMapper;
import br.com.applogin.backend_applogin.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService users;
    private final JwtUtil jwt;
    private final UserMapper mapper;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid AuthRegisterRequest req) {
        User u = users.register(req.username(), req.email(), req.password(), Set.of("USER"));
        return ResponseEntity.ok(mapper.toResponse(u));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthLoginRequest req) {
        var uopt = users.findByUsernameOrEmail(req.usernameOrEmail());
        if (uopt.isEmpty() || !users.checkPassword(uopt.get(), req.password())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        var u = uopt.get();
        var roles = u.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());
        String token = jwt.generate(u.getUsername(), roles);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "tokenType", "Bearer",
                "expiresInSeconds", jwt.getExpirationSeconds(),
                "user", mapper.toResponse(u)));
    }
}
