package com.zack.friendshub.service;

import com.zack.friendshub.enums.Role;
import com.zack.friendshub.enums.UserStatus;
import com.zack.friendshub.model.User;
import com.zack.friendshub.model.UserIntegration;
import com.zack.friendshub.repository.UserIntegrationRepo;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GoogleOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserIntegrationRepo userIntegrationRepo;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String name = oauthToken.getPrincipal().getAttribute("name");
        String email = oauthToken.getPrincipal().getAttribute("email");

        User user = userRepo.findByEmail(email).orElseGet(() -> {
            String generatedUsername = email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 5);
            User newUser = User.builder()
                    .username(generatedUsername)
                    .name(name)
                    .email(email)
                    .role(Role.USER)
                    .status(UserStatus.ACTIVATED)
                    .dateOfRegistration(LocalDateTime.now())
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .build();
            return userRepo.save(newUser);
        });
        OAuth2AuthorizedClient client = authorizedClientService
                .loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());

        if (client != null && client.getRefreshToken() != null) {
            String refreshToken = client.getRefreshToken().getTokenValue();
            Optional<UserIntegration> userIntegration = userIntegrationRepo.findByUserIdAndProvider(user.getId(), "GOOGLE");

            if (userIntegration.isPresent()) {
                userIntegration.get().setRefreshToken(refreshToken);
                userIntegrationRepo.save(userIntegration.get());
            } else {
                UserIntegration newUserIntegration = UserIntegration.builder()
                        .user(user)
                        .provider("GOOGLE")
                        .refreshToken(refreshToken)
                        .createdAt(LocalDateTime.now())
                        .build();

                userIntegrationRepo.save(newUserIntegration);
            }
        }
        String jwtToken = jwtUtil.generateToken(user.getUsername());
        response.sendRedirect("/api/auth/oauth2/success?token=" + jwtToken);
    }
}
