package com.ecommerce.order.dto;


import com.ecommerce.order.model.UserRole;
import lombok.Data;

@Data
public class UserResponse {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private AddressDTO address;
}
