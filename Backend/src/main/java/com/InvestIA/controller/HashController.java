package com.InvestIA.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hash")
public class HashController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/generate/{password}")
    public ResponseEntity<String> generateHash(@PathVariable String password) {
        String hash = passwordEncoder.encode(password);
        return ResponseEntity.ok("Hash para '" + password + "': " + hash);
    }
}