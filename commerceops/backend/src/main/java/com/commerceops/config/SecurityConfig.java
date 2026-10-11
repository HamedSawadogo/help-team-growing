package com.commerceops.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

// No custom PasswordEncoder bean here: this lab only relies on Spring Boot's
// auto-configured in-memory user (spring.security.user.*) for now, which
// manages its own encoding. A real user/role model is still future work.
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/actuator/**").authenticated()
                .requestMatchers("/api/admin/**").authenticated()
                .requestMatchers("/api/**").permitAll()
                .anyRequest().authenticated())
            .httpBasic(withDefaults -> {});
        return http.build();
    }
}
