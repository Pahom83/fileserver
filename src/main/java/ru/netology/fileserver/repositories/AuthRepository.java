package ru.netology.fileserver.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.netology.fileserver.utils.JWTTokenUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Repository
@RequiredArgsConstructor
public class AuthRepository {
    private final JWTTokenUtil util;
    Map<String, String> authorizedUsers = new ConcurrentHashMap<>();

    public void addUser(String token, String username){
        authorizedUsers.putIfAbsent(util.getTokenFromRawToken(token), username);
    }

    public void removeUser(String token){
        authorizedUsers.remove(util.getTokenFromRawToken(token));
    }
}
