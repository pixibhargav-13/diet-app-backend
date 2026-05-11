package com.dietapp.usermanagement.service;

import com.dietapp.api.usermanagement.model.MessageResponse;
import com.dietapp.usermanagement.exception.InvalidTokenException;
import com.dietapp.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MessageResponse forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(Instant.now().plusSeconds(3600)); // 1 hour
            userRepository.save(user);
            // TODO: Send email with reset link: /reset-password?token={token}
            // Dev: find the token in DB — SELECT password_reset_token FROM users WHERE email = '...';
        });
        // Always return the same message to prevent email enumeration
        return new MessageResponse().message("If that email is registered, a password reset link has been sent.");
    }

    @Transactional
    public MessageResponse resetPassword(String token, String newPassword) {
        var user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token."));

        if (user.getPasswordResetTokenExpiry() == null
                || user.getPasswordResetTokenExpiry().isBefore(Instant.now())) {
            throw new InvalidTokenException("Reset token has expired.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
        return new MessageResponse().message("Password reset successfully. You can now log in.");
    }
}
