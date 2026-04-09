package com.ecommerce.user.service;


import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.model.Address;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KeyCloakAdminService keyCloakAdminService;

    public List<UserResponse> getAllUsers(){
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse createUser(UserRequest request){
        // creating user in keycloak
        String token = keyCloakAdminService.getAdminAccessToken();
        String keycloakUserId = keyCloakAdminService.createUser(token, request);

        // to create user in database
        User user = this.mapToUser(request);
        user.setKeycloakId(keycloakUserId);
        keyCloakAdminService.assignRealmRoleToUser(request.getUsername(), "USER", keycloakUserId);

        User savedUser = userRepository.save(user);

        return this.mapToUserResponse(savedUser);
    }

    public UserResponse findUserById(String userId) {
        User exitingUser = userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User with the Id: " + userId + " does not exist"));

        return this.mapToUserResponse(exitingUser);
    }

    public UserResponse updateUser(String userId, UserRequest user) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User not found"));

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        userRepository.save(existingUser);

        return mapToUserResponse(existingUser);
    }

    private User mapToUser(UserRequest request){
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null){
            Address address = new Address();
            address.setStreet(request.getAddress().getStreet());
            address.setCity(request.getAddress().getCity());
            address.setState(request.getAddress().getState());
            address.setZipCode(request.getAddress().getZipCode());
            address.setCountry(request.getAddress().getCountry());
            user.setAddress(address);
        }
        return user;
    }

    private UserResponse mapToUserResponse(User user){
        UserResponse response = new UserResponse();
        response.setKeycloakId(user.getKeycloakId());
        response.setUserId(String.valueOf(user.getUserId()));
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole());
        if (user.getAddress() != null){
            AddressDTO address = new AddressDTO();
            address.setStreet(user.getAddress().getStreet());
            address.setCity(user.getAddress().getCity());
            address.setState(user.getAddress().getState());
            address.setZipCode(user.getAddress().getZipCode());
            address.setCountry(user.getAddress().getCountry());
            response.setAddress(address);
        }
        return response;
    }
}
