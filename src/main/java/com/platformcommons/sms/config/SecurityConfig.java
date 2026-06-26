package com.platformcommons.sms.config;

import com.platformcommons.sms.entity.UserCredential;
import com.platformcommons.sms.repository.UserCredentialRepository;
import com.platformcommons.sms.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthFilter, AuthorizationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/students/register").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/students/courses/create").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/students/enroll-student-in-course").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/students/search-by-name").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/students/enrolled-in-course/**").hasRole("ADMIN")


                        .requestMatchers(HttpMethod.GET, "/api/v1/students/{studentId}/profile").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/students/{studentId}/update-profile").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/v1/students/courses/search-by-name").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/v1/students/courses/search-by-topic").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/students/{studentId}/unenroll-from-course/{courseId}").hasRole("STUDENT")

                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserCredentialRepository userCredentialRepository) {
        return username -> {
            UserCredential user = userCredentialRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .authorities(user.getRole())
                    .build();
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
