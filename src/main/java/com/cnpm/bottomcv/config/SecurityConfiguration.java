package com.cnpm.bottomcv.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**",
                                "/api/front/jobs/**",
                                "/api/front/reviews/**",
                                "/api/front/companies/**",
                                "/api/front/categories/**",
                                "/api/info/**",
                                "/actuator/**",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/configuration/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**").permitAll()
                        .requestMatchers("/api/front/applies/**").hasRole("CANDIDATE")
                        .requestMatchers("/api/front/cvs/**").hasRole("CANDIDATE")
                        .requestMatchers("/api/front/users/**").hasAnyRole("CANDIDATE", "EMPLOYER")
                        .requestMatchers("/api/front/notifications/**").hasAnyRole("CANDIDATE", "EMPLOYER")
                        // Back APIs (dashboard)
                        .requestMatchers("/api/back/jobs/**").hasAnyRole("EMPLOYER", "ADMIN")
                        .requestMatchers("/api/back/applies/**").hasAnyRole("EMPLOYER", "ADMIN")
                        .requestMatchers("/api/back/cvs/**").hasAnyRole("EMPLOYER", "ADMIN")
                        .requestMatchers("/api/back/reviews/**").hasRole("ADMIN")
                        .requestMatchers("/api/back/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/back/notifications/**").hasAnyRole("EMPLOYER", "ADMIN")
                        .requestMatchers("/api/back/categories/**").hasRole("ADMIN")
                        .requestMatchers("/api/back/companies/**").hasAnyRole("EMPLOYER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}