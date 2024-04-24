package ru.netology.fileserver.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.netology.fileserver.dto.requests.AuthRequest;
import ru.netology.fileserver.dto.responses.AuthResponse;
import ru.netology.fileserver.entities.User;
import ru.netology.fileserver.repositories.AuthRepository;
import ru.netology.fileserver.utils.JWTTokenUtil;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthServiceTest {
    @InjectMocks
    private AuthService mockAuthService;
    @Mock
    AuthRepository mockAuthRepository;
    @Mock
    private MyUserService mockUserService;
    @Mock
    private AuthenticationManager mockAuthenticationManager;
    @Mock
    private JWTTokenUtil mockTokenUtil;

    AuthRequest authRequest;
    User user;
    @BeforeEach
    void settings() {
        authRequest = new AuthRequest("test1", "password");
        user = new User();
        user.setUsername(authRequest.login());
        user.setPassword(new BCryptPasswordEncoder().encode(authRequest.password()));
    }

    @Test
    void testCreateAuthToken(){
        when(mockAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.login(), authRequest.password())))
                .thenReturn(any());
        when(mockUserService.loadUserByUsername(authRequest.login())).thenReturn(user);
        when(mockTokenUtil.generateToken(user)).thenReturn("123456789");
        assert(Objects.equals(mockAuthService.createAuthToken(authRequest), ResponseEntity.ok(new AuthResponse("123456789"))));
    }

}
