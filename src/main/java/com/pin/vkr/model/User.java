package com.pin.vkr.model;

import lombok.Data;
import java.util.List;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String role; // ROLE_CLIENT или ROLE_ADMIN
    private String email;
    private String phone;
    private String address;
}
