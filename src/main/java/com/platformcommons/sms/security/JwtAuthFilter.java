package com.platformcommons.sms.security;

import com.platformcommons.sms.entity.UserCredential;
import com.platformcommons.sms.repository.UserCredentialRepository;
import com.platformcommons.sms.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import java.util.Collections;

import java.io.IOException;
import java.util.Optional;
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserCredentialRepository userCredentialRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            try {
                String tokenVal = token.substring(7);
                String username = jwtService.getUsername(tokenVal);
                Optional<UserCredential> byUsername = userCredentialRepository.findByUsername(username);
                if (byUsername.isPresent()) {
                    UserCredential userCredential = byUsername.get();
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userCredential, null,
                                    Collections.singleton(new SimpleGrantedAuthority(userCredential.getRole()))
                            );
                    authToken.setDetails(new WebAuthenticationDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                log.warn("JWT validation failed: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
