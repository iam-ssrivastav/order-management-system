package com.enterprise.gateway.controller;

import com.enterprise.gateway.util.JwtUtil;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Mono<String> login(@RequestParam String username) {
        // In a real app, validate password here
        // Assign roles based on username pattern
        List<String> roles;
        String lowerUsername = username.toLowerCase();

        if (lowerUsername.contains("admin")) {
            // ADMIN has all roles
            roles = Arrays.asList("ADMIN", "MANAGER", "FINANCE", "AUDITOR", "SUPPORT", "WAREHOUSE", "USER");
        } else if (lowerUsername.contains("manager")) {
            // MANAGER has management + auditor + support capabilities
            roles = Arrays.asList("MANAGER", "AUDITOR", "SUPPORT", "USER");
        } else if (lowerUsername.contains("finance")) {
            // FINANCE has financial operations + auditor
            roles = Arrays.asList("FINANCE", "AUDITOR", "USER");
        } else if (lowerUsername.contains("support")) {
            // SUPPORT has customer support capabilities
            roles = Arrays.asList("SUPPORT", "USER");
        } else if (lowerUsername.contains("warehouse")) {
            // WAREHOUSE has logistics capabilities
            roles = Arrays.asList("WAREHOUSE", "USER");
        } else if (lowerUsername.contains("auditor")) {
            // AUDITOR has read-only access to everything
            roles = Arrays.asList("AUDITOR", "USER");
        } else {
            // Default USER role
            roles = Arrays.asList("USER");
        }

        return Mono.just(jwtUtil.generateToken(username, roles));
    }
}
