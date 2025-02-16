package com.tmeras.resellmart.security;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.token.JwtService;
import com.tmeras.resellmart.token.Token;
import com.tmeras.resellmart.token.TokenRepository;
import com.tmeras.resellmart.token.TokenType;
import com.tmeras.resellmart.user.User;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LogoutServiceTests {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    public void shouldLogoutUserWhenValidRequest() {
        User user = TestDataUtils.createUserA(Set.of(new Role(1, "USER")));
        String accessToken = "accessToken";
        Token refreshToken = new Token(null, "refreshToken", TokenType.BEARER, LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().plusMinutes(1), null, false, user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + accessToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, user.getPassword(), user.getAuthorities()
        );

        when(tokenRepository.existsByToken(accessToken)).thenReturn(false);
        when(jwtService.extractUsername(accessToken)).thenReturn(user.getEmail());
        when(tokenRepository.findAllValidRefreshTokensByUserEmail(user.getEmail())).thenReturn(List.of(refreshToken));

        logoutService.logout(request, response, authentication);

        assertThat(refreshToken.isRevoked()).isTrue();
    }

    @Test
    public void shouldNotLogoutUserWhenMissingAccessToken() throws IOException {
        User user = TestDataUtils.createUserA(Set.of(new Role(1, "USER")));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, user.getPassword(), user.getAuthorities()
        );
        String expectedResponse = "{\"error\": \"No access token in Bearer header\"}";

        logoutService.logout(request, response, authentication);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldNotLogoutUserWhenAccessTokenIsSent() throws IOException {
        User user = TestDataUtils.createUserA(Set.of(new Role(1, "USER")));
        Token refreshToken = new Token(null, "refreshToken", TokenType.BEARER, LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().plusMinutes(1), null, false, user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + refreshToken.getToken());
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, user.getPassword(), user.getAuthorities()
        );
        String expectedResponse = "{\"error\": \"Invalid access token\"}";

        when(tokenRepository.existsByToken(refreshToken.getToken())).thenReturn(true);

        logoutService.logout(request, response, authentication);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldNotLogoutUserWhenInvalidAccessToken() throws IOException {
        User user = TestDataUtils.createUserA(Set.of(new Role(1, "USER")));
        String accessToken = "accessToken";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + accessToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, user.getPassword(), user.getAuthorities()
        );
        String expectedResponse = "{\"error\": \"Invalid access token\"}";

        when(tokenRepository.existsByToken(accessToken)).thenReturn(false);
        when(jwtService.extractUsername(accessToken)).thenThrow(new JwtException("Invalid access token"));

        logoutService.logout(request, response, authentication);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).isEqualTo(expectedResponse);
    }
}
