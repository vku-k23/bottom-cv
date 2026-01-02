package com.cnpm.bottomcv.config;

import com.cnpm.bottomcv.constant.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {
        private final AuthenticationProvider authenticationProvider;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.cors(Customizer.withDefaults())
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/v1/auth/**",
                                                                "/api/v1/public/**",
                                                                "/api/v1/info/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui.html", "/swagger-ui/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/v1/front/categories/**",
                                                                "/api/v1/front/jobs/**",
                                                                "/api/v1/front/reviews/**",
                                                                "/api/v1/front/companies/**")
                                                .permitAll()
                                                // Specific rules for EMPLOYER and ADMIN
                                                .requestMatchers(
                                                                "/api/v1/back/admin/**", // AdminDashboardController
                                                                "/api/v1/back/jobs/**",
                                                                "/api/v1/back/companies/**",
                                                                "/api/v1/back/settings/**",
                                                                "/api/v1/back/applies/**")
                                                .hasAnyRole(RoleType.ADMIN.name(), RoleType.EMPLOYER.name())
                                                // Allow EMPLOYER and ADMIN to access applies endpoints (for job
                                                // applications management)
                                                .requestMatchers("/api/v1/applies/**")
                                                .hasAnyRole(RoleType.CANDIDATE.name(), RoleType.EMPLOYER.name(),
                                                                RoleType.ADMIN.name())
                                                // Allow EMPLOYER to GET categories (for job posting), but only ADMIN
                                                // can modify
                                                .requestMatchers(HttpMethod.GET, "/api/v1/back/categories/**")
                                                .hasAnyRole(RoleType.ADMIN.name(), RoleType.EMPLOYER.name())
                                                .requestMatchers("/api/v1/back/categories/**")
                                                .hasRole(RoleType.ADMIN.name())
                                                .requestMatchers("/api/v1/back/users/**").hasRole(RoleType.ADMIN.name())
                                                .requestMatchers("/api/v1/back/moderation/**")
                                                .hasRole(RoleType.ADMIN.name())
                                                .requestMatchers("/api/v1/back/reports/**")
                                                .hasRole(RoleType.ADMIN.name())
                                                .requestMatchers("/api/v1/back/payments/**")
                                                .hasRole(RoleType.ADMIN.name())
                                                .requestMatchers("/api/v1/back/**").hasRole(RoleType.ADMIN.name()) // Catch-all
                                                                                                                   // for
                                                                                                                   // other
                                                                                                                   // ADMIN-only
                                                                                                                   // back
                                                                                                                   // endpoints
                                                // Other rules
                                                .requestMatchers("/api/v1/front/**")
                                                .hasAnyRole(RoleType.CANDIDATE.name(), RoleType.EMPLOYER.name())
                                                // File download/upload endpoints - require authentication
                                                .requestMatchers("/api/v1/files/**").authenticated()
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

}