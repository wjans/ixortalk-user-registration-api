package com.ixortalk.user.registration.api.config;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixortalk.user.registration.api.feign.OAuth2FeignRequestInterceptor;
import feign.Logger;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

public class FeignConfiguration {

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails;

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Decoder feignDecoder() {
        return new JacksonDecoder(objectMapper);
    }

    @Bean
    public Encoder feignEncoder() {
        return new JacksonEncoder(objectMapper);
    }

    @Bean
    public OAuth2FeignRequestInterceptor requestInterceptor() {
        return new OAuth2FeignRequestInterceptor(new OAuth2RestTemplate(oAuth2ProtectedResourceDetails));
    }
}
