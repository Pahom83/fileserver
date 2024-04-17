package ru.netology.fileserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.fileserver.dto.requests.AuthRequest;
import ru.netology.fileserver.dto.responses.LogoutResponse;
import ru.netology.fileserver.services.AuthService;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/login")
    public ResponseEntity<?> createAuthToken(@RequestBody AuthRequest request){
        return authService.createAuthToken(request);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> removeAuthToken(@RequestHeader("auth-token") String token){
        return ResponseEntity.ok(authService.removeAuthToken(token));
    }

    @GetMapping("/login")
    public ResponseEntity<?> logout(){
        return ResponseEntity.ok(new LogoutResponse("true"));
    }

}