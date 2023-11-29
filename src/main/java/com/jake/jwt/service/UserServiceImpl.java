package com.jake.jwt.service;

import com.jake.jwt.dto.UserRequest;
import com.jake.jwt.dto.UserResponse;
import com.jake.jwt.model.UserInfo;
import com.jake.jwt.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public UserResponse saveUser(UserRequest userRequest) {

        if(userRequest.getUsername() == null || userRequest.getPassword() == null) {
            throw new IllegalArgumentException("Check Parameter. username or password not found");
        }

        UserInfo savedUser = null;

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = userRequest.getPassword();
        String encodedPassword = encoder.encode(rawPassword);

        UserInfo user = modelMapper.map(userRequest, UserInfo.class);
        user.setPassword(encodedPassword);

        if(userRequest.getId() != null) {
            UserInfo foundUser = userRepository.findFirstById(userRequest.getId());
            if(foundUser != null) {
                foundUser.setId(user.getId());
                foundUser.setUsername(user.getUsername());
                foundUser.setPassword(user.getPassword());
                foundUser.setRoles(user.getRoles());

                savedUser = userRepository.save(foundUser);
                userRepository.refresh(savedUser);
            } else {
                throw new RuntimeException("Record not found : " + userRequest.getId());
            }
        } else {
            savedUser = userRepository.save(user);
        }
        userRepository.refresh(savedUser);
        return modelMapper.map(savedUser, UserResponse.class);
    }

    @Override
    public UserResponse getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String usernameFromAccessToken = userDetails.getUsername();
        UserInfo userInfo = userRepository.findByUsername(usernameFromAccessToken);
        return modelMapper.map(userInfo, UserResponse.class);
    }

    @Override
    public List<UserResponse> getAllUser() {

        List<UserInfo> users = (List<UserInfo>) userRepository.findAll();
        Type setOfDtoType = new TypeToken<List<UserResponse>>(){}.getType();

        return modelMapper.map(users, setOfDtoType);
    }
}
