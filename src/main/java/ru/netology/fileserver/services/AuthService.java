package ru.netology.fileserver.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.netology.fileserver.dto.requests.AuthRequest;
import ru.netology.fileserver.dto.responses.AuthResponse;
import ru.netology.fileserver.dto.Exception;
import ru.netology.fileserver.repositories.AuthRepository;
import ru.netology.fileserver.utils.JWTTokenUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MyUserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTTokenUtil tokenUtil;
    private final AuthRepository authRepository;

    public ResponseEntity<?> createAuthToken(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.login(), authRequest.password()));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new Exception(HttpStatus.BAD_REQUEST.value(), "Неправильный логин или пароль"), HttpStatus.BAD_REQUEST);
        }
        UserDetails user = userService.loadUserByUsername(authRequest.login());
        String token = tokenUtil.generateToken(user);
        authRepository.addUser(token, authRequest.login());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    public ResponseEntity<?> removeAuthToken(String token) {
        authRepository.removeUser(token);
        return ResponseEntity.ok(HttpEntity.EMPTY);
    }
}
