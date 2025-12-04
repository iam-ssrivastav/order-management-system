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
        // Assign roles based on username
        List<String> roles = username.toLowerCase().contains("admin")
                ? Arrays.asList("ADMIN", "USER")
                : Arrays.asList("USER");

        return Mono.just(jwtUtil.generateToken(username, roles));
    }
}
