package br.com.applogin.backend_applogin.service.impl;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.applogin.backend_applogin.domain.entity.User;
import br.com.applogin.backend_applogin.domain.repository.RoleRepository;
import br.com.applogin.backend_applogin.domain.repository.UserRepository;
import br.com.applogin.backend_applogin.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public User register(String username, String email, String rawPassword, Set<String> rolesName) {
        if (users.existsByUsername(username))
            throw new IllegalArgumentException("Username already taken");
        if (users.existsByEmail(email))
            throw new IllegalArgumentException("Email already registered");

        var roleEntities = rolesName.stream()
                .map(rn -> roles.findByName(rn)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + rn)))
                .collect(Collectors.toSet());

        var user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(encoder.encode(rawPassword))
                .enabled(true)
                .roles(roleEntities)
                .build();

        return users.save(user);
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return users.findByUsername(usernameOrEmail)
                .or(() -> users.findByEmail(usernameOrEmail));
    }

    @Override
    public boolean checkPassword(User user, String rawPassword) {
        return encoder.matches(rawPassword, user.getPasswordHash());
    }

}
