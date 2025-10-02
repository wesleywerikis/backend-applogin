package br.com.applogin.backend_applogin.service;

import java.util.Optional;
import java.util.Set;

import br.com.applogin.backend_applogin.domain.entity.User;

public interface UserService {
    User register(String username, String email, String rawPassword, Set<String> roles);
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
    boolean checkPassword(User user, String rawPassword);
}
