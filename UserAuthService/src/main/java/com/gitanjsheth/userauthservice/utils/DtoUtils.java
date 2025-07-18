package com.gitanjsheth.userauthservice.utils;

import com.gitanjsheth.userauthservice.dtos.RoleDto;
import com.gitanjsheth.userauthservice.dtos.UserDto;
import com.gitanjsheth.userauthservice.models.Role;
import com.gitanjsheth.userauthservice.models.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


public class DtoUtils {
    
    public static UserDto convertToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setCreatedAt(user.getCreatedAt());

        if (user.getRoles() != null) {
            List<RoleDto> roleDtos = user.getRoles().stream()
                    .map(DtoUtils::convertToRoleDto)
                    .collect(Collectors.toList());
            userDto.setRoles(roleDtos);
        }

        return userDto;
    }
    
    public static RoleDto convertToRoleDto(Role role) {
        RoleDto roleDto = new RoleDto();
        roleDto.setId(role.getId());
        roleDto.setRoleName(role.getRoleName());
        roleDto.setRoleDescription(role.getRoleDescription());
        return roleDto;
    }
} 