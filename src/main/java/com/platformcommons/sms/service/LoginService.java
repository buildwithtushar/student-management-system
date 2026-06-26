package com.platformcommons.sms.service;

import com.platformcommons.sms.dto.AuthResponse;
import com.platformcommons.sms.dto.LoginRequest;
import com.platformcommons.sms.entity.UserCredential;
import com.platformcommons.sms.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final JWTService jwtService;
    private final UserCredentialRepository userCredentialRepository;

    public AuthResponse verifyLogin(LoginRequest loginRequest){
        Optional<UserCredential> byUsername = userCredentialRepository.findByUsername(loginRequest.getUsername());

        if (byUsername.isPresent()) {
            UserCredential user = byUsername.get();

            System.out.println("DEBUG - DB hash: " + user.getPassword());
            System.out.println("DEBUG - checkpw result: " + BCrypt.checkpw(loginRequest.getPassword(), user.getPassword()));

            if (BCrypt.checkpw(loginRequest.getPassword(), user.getPassword())) {
                String token = jwtService.generateToken(user.getUsername(), user.getRole());
                String cleanRole = user.getRole().replace("ROLE_", "");
                return new AuthResponse(token, cleanRole);
            }
        }
        return null;
    }
}
