package com.gitanjsheth.userauthservice.dtos;

import com.gitanjsheth.userauthservice.models.Status;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private List<RoleDto> roles;
    private Status status;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;
}
