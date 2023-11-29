package com.jake.jwt.service;

import com.jake.jwt.dto.UserRequest;
import com.jake.jwt.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse saveUser(UserRequest userRequest);

    UserResponse getUser();

    List<UserResponse> getAllUser();
}
