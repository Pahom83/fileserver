package ru.netology.fileserver.configs;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.netology.fileserver.exceptions.UnauthorizedException;
import ru.netology.fileserver.services.MyUserService;
import ru.netology.fileserver.utils.JWTTokenUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestFilter extends OncePerRequestFilter {
    private final JWTTokenUtil jwtTokenUtil;
    private final MyUserService userService;
    @Override
    protected void doFilterInternal(HttpServletRequest request
            , HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("auth-token");
//        System.out.println(authHeader);
        String username = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsername(jwt);
            } catch (ExpiredJwtException e) {
                log.error("Время жизни токена вышло");
                throw new UnauthorizedException("Время жизни токена вышло");
            } catch (SignatureException e) {
                log.error("Подпись неправильная");
                throw new UnauthorizedException("Подпись неправильная");
            }
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(token);
        }
        filterChain.doFilter(request, response);
    }
}
