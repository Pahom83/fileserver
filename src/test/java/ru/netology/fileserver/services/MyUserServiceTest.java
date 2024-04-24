package ru.netology.fileserver.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.netology.fileserver.entities.User;
import ru.netology.fileserver.repositories.UserRepository;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MyUserServiceTest {

    @InjectMocks
    private MyUserService mockMyUserService;
    @Mock
    private UserRepository mockUserRepository;

    User user;
    @BeforeEach
    void settings() {
        user = new User();
        user.setUsername("user1");
        user.setPassword(new BCryptPasswordEncoder().encode("password"));
    }

    @Test
    void loadUserByUsernameTest(){
        when(mockUserRepository.findByUsername("user1")).thenReturn(Optional.ofNullable(user));
        assert(mockMyUserService.loadUserByUsername("user1").equals(user));
    }


}
