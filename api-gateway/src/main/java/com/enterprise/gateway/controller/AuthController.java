package com.enterprise.gateway.controller;

import com.enterprise.gateway.util.JwtUtil;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
        return Mono.just(jwtUtil.generateToken(username));
    }
}
