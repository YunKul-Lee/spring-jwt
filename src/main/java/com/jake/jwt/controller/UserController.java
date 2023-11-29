package com.jake.jwt.controller;

import com.jake.jwt.dto.*;
import com.jake.jwt.model.RefreshToken;
import com.jake.jwt.service.JwtService;
import com.jake.jwt.service.RefreshTokenService;
import com.jake.jwt.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService, JwtService jwtService, RefreshTokenService refreshTokenService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping(value = "/save")
    public ResponseEntity<UserResponse> saveUser(@RequestBody UserRequest userRequest) {
        UserResponse userResponse = userService.saveUser(userRequest);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> userResponses = userService.getAllUser();
        return ResponseEntity.ok(userResponses);
    }

    @PostMapping("/profile")
    public ResponseEntity<UserResponse> getUserProfile() {
        UserResponse userResponse = userService.getUser();
        return ResponseEntity.ok(userResponse);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin")
    public String checkAdminRole() {
        return "Welcome";
    }

    @PostMapping("/login")
    public JwtResponseDto authenticateAndGetToken(@RequestBody AuthRequestDto authRequestDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDto.getUsername(), authRequestDto.getPassword()));
        if(authentication.isAuthenticated()) {
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDto.getUsername());
            return JwtResponseDto.builder()
                    .accessToken(jwtService.generateToken(authRequestDto.getUsername()))
                    .token(refreshToken.getToken())
                    .build();
        } else {
            throw new UsernameNotFoundException("Login Fail. Invalid Request");
        }

    }

    @PostMapping("/refreshToken")
    public JwtResponseDto refreshToken(@RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        return refreshTokenService.findByToken(refreshTokenRequestDto.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(userInfo -> {
                    String accessToken = jwtService.generateToken(userInfo.getUsername());
                    return JwtResponseDto.builder()
                            .accessToken(accessToken)
                            .token(refreshTokenRequestDto.getToken())
                            .build();
                }).orElseThrow(() -> new RuntimeException("RefreshToken not found."));
    }
}

