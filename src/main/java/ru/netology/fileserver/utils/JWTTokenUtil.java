package ru.netology.fileserver.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTTokenUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Value("${jwt.lifetime}")
    private Duration jwtLifetime;

    // генерация токена (кладем в него имя пользователя и время действия токена)
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        Date createDate = new Date();
        Date expiredDate = new Date(createDate.getTime() + jwtLifetime.toMillis());
        System.out.println(userDetails.getUsername());
        return Jwts.builder()
                .addClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(createDate)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    //достаем имя пользователя из токена
    public String getUsername(String token) {
        return extractClaimsFromToken(token).getSubject();
    }


    private Claims extractClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(getTokenFromRawToken(token))
                .getBody();
    }

    public String getTokenFromRawToken(String rawToken) {
        if (rawToken.startsWith("Bearer")){
            return rawToken.substring(7);
        }
        return rawToken;
    }
}