package com.kudos.config;

import com.kudos.security.JwtAuthenticationFilter;
import com.kudos.service.CustomAdminDetailsService; // <-- implement this
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final CustomAdminDetailsService customAdminDetailsService;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.cors(cors -> cors.configurationSource(request -> {
                        var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                        corsConfig.setAllowedOrigins(List.of("http://localhost:3000"));
                        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                        corsConfig.setAllowedHeaders(List.of("Authorization", "Content-Type"));
                        corsConfig.setAllowCredentials(true);
                        return corsConfig;
                })).csrf(csrf -> csrf.disable()) // disable CSRF temporarily
                                                 // .authorizeRequests()
                                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // Swagger UI and OpenAPI docs
                                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                                                                "/swagger-ui.html", "/api/employee/all")
                                                .permitAll()
                                                // Public employee endpoints
                                                .requestMatchers("/api/login", "/api/auth/login", "/api/public/**",
                                                                "/api/employee/login", "/api/employee/register",
                                                                "/api/kudos/recent",
                                                                "/api/kudos/employee/{employeeId}/history",
                                                                "/api/kudos/employee/{employeeId}/kudos-count",
                                                                "/api/kudos/team/{teamId}/history",
                                                                "/api/kudos/leaderboard/employees",
                                                                "/api/kudos/leaderboard/teams", "/api/admin/login",
                                                                "/api/employee/all", "/api/employee/teams",
                                                                "/leaderboard", "/api/employee/me",
                                                                "/api/kudos/public/history",
                                                                "/api/kudos/employee/*/history",
                                                                "/api/kudos/team/*/history")
                                                .permitAll()
                                                // Admin-only endpoints
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                // Everything else must be authenticated
                                                .anyRequest().authenticated())
                                .formLogin(fl -> fl.disable()).httpBasic(b -> b.disable());

                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        // This bean allows Spring Security to authenticate using your custom admin
        // service
        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(customAdminDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
