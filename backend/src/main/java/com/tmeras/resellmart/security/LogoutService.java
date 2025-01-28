package com.tmeras.resellmart.security;

import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.user.User;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;

    private final JwtService jwtService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"No access token in Bearer header\"}");
                return;
            }

            // Ensure refresh token wasn't sent instead of access token
            final String accessToken = authHeader.substring(7);
            if (tokenRepository.existsByToken(accessToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid access token\"}");
                return;
            }

            // Invalidate all of the user's refresh tokens
            try {
                String userEmail = jwtService.extractUsername(accessToken);
                List<Token> tokens = tokenRepository.findAllValidRefreshTokensByUserEmail(userEmail);
                tokens.forEach(token -> token.setRevoked(true));
                tokenRepository.saveAll(tokens);
            }
            catch (JwtException ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid access token\"}");
            }
        }
        catch (IOException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
