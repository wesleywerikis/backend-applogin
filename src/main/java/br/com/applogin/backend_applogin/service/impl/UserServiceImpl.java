package br.com.applogin.backend_applogin.service.impl;

import java.util.Optional;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.applogin.backend_applogin.domain.entity.Role;
import br.com.applogin.backend_applogin.domain.entity.User;
import br.com.applogin.backend_applogin.domain.repository.UserRepository;
import br.com.applogin.backend_applogin.service.UserService;
import jakarta.persistence.EntityManager;

public class UserServiceImpl implements UserService {

    private final UserRepository users;
    private final EntityManager em;
    private final PasswordEncoder encoder;

    @Override
    public User register(String username, String email, String rawPassword, Set<String> roles) {
        if (users.existsByUsername(username))
            throw new IllegalArgumentException("Username already taken");
        if (users.existsByEmail(email))
            throw new IllegalArgumentException("Email already registered");

        var user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(encoder.encode(rawPassword))
                .enabled(true)
                .build();

        var roleEntities = roles.stream()
                .map(rn -> em.createQuery(
                        "SELECT r FROM Role r WHERE r.name = :n", Role.class)
                        .setParameter("n", rn).getResultStream().findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + rn)))
                .collect(java.util.stream.Collectors.toSet());

        user.setRoles(roleEntities);
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
