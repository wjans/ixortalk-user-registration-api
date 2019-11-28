package com.ixortalk.user.registration.api.config;

import com.auth0.spring.security.api.authentication.JwtAuthentication;
import com.ixortalk.autoconfigure.oauth2.IxorTalkHttpSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@Configuration
public class TestSecurityConfig {

    @Bean
    public IxorTalkHttpSecurityConfigurer dummyAuth0AuthenticationProvider() {
        return http -> http.authenticationProvider(new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                return authentication;
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return JwtAuthentication.class.isAssignableFrom(authentication);
            }
        });
    }
}
