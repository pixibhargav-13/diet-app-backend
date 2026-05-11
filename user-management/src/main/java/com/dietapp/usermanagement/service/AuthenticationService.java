package com.dietapp.usermanagement.service;

import com.dietapp.api.usermanagement.model.AuthResponse;
import com.dietapp.usermanagement.exception.UserAlreadyExistsException;
import com.dietapp.usermanagement.mapper.UserMapper;
import com.dietapp.usermanagement.model.entity.UserEntity;
import com.dietapp.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserMapper userMapper;
    private final DatabaseUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(String firstName, String lastName, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already registered: " + email);
        }

        UserEntity user = UserEntity.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .build();
        user = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new AuthResponse()
                .accessToken(jwtTokenService.generateAccessToken(userDetails))
                .refreshToken(jwtTokenService.generateRefreshToken(userDetails))
                .tokenType("Bearer")
                .user(userMapper.toUserResponse(user));
    }

    public AuthResponse login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow();
        return new AuthResponse()
                .accessToken(jwtTokenService.generateAccessToken(userDetails))
                .refreshToken(jwtTokenService.generateRefreshToken(userDetails))
                .tokenType("Bearer")
                .user(userMapper.toUserResponse(userEntity));
    }
}
