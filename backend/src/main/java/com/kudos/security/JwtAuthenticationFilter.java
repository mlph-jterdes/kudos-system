package com.kudos.security;

import com.kudos.model.Admin;
import com.kudos.model.Employee;
import com.kudos.service.AdminService;
import com.kudos.service.CustomUserDetailsService;
import com.kudos.service.EmployeeService;
import com.kudos.security.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AdminService adminService;
    private final EmployeeService employeeService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // ---------------- GET AUTHORIZATION HEADER ----------------
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        // ---------------- EXTRACT JWT TOKEN ----------------
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Remove "Bearer " prefix
            try {
                email = jwtUtil.extractEmail(token);
            } catch (Exception e) {
                // Could log invalid token attempt
                e.printStackTrace();
            }
        }

        // ---------------- VALIDATE TOKEN & SET AUTHENTICATION ----------------
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Check if user is ADMIN
            Optional<Admin> adminOpt = adminService.findByEmail(email);
            if (adminOpt.isPresent() && jwtUtil.validateToken(token, email)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        new User(email, "", Collections.emptyList()), null, Collections.singleton(() -> "ROLE_ADMIN"));
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // Make email & role available to controllers
                request.setAttribute("email", email);
                request.setAttribute("role", "ADMIN");

            } else {
                // Check if user is EMPLOYEE
                Optional<Employee> empOpt = employeeService.findByEmail(email);
                if (empOpt.isPresent() && jwtUtil.validateToken(token, email)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            new User(email, "", Collections.emptyList()), null,
                            Collections.singleton(() -> "ROLE_EMPLOYEE"));
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Make email & role available to controllers
                    request.setAttribute("email", email);
                    request.setAttribute("role", "EMPLOYEE");
                }
            }
        }

        // ---------------- CONTINUE FILTER CHAIN ----------------
        filterChain.doFilter(request, response);
    }
}
