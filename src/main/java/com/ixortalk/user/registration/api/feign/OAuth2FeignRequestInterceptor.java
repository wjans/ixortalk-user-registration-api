package com.ixortalk.user.registration.api.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;

import static org.springframework.security.oauth2.common.OAuth2AccessToken.BEARER_TYPE;

public class OAuth2FeignRequestInterceptor implements RequestInterceptor {
 
    private OAuth2RestTemplate oAuth2RestTemplate;

    public OAuth2FeignRequestInterceptor(OAuth2RestTemplate oAuth2RestTemplate) {
        this.oAuth2RestTemplate = oAuth2RestTemplate;
    }

    @Override
    public void apply(RequestTemplate template) {
         template.header(HttpHeaders.AUTHORIZATION, String.format("%s %s", BEARER_TYPE, oAuth2RestTemplate.getAccessToken().getValue()));
    }
}