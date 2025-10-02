package br.com.applogin.backend_applogin.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.applogin.backend_applogin.domain.entity.Role;
import br.com.applogin.backend_applogin.domain.entity.User;
import br.com.applogin.backend_applogin.dto.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserResponse toResponse(User user);

    default Set<String> mapRoles(Set<Role> roles) {
        return roles == null ? Set.of() : roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}