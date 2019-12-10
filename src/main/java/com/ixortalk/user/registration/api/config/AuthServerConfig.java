/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-present IxorTalk CVBA
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ixortalk.user.registration.api.config;

import com.ixortalk.autoconfigure.oauth2.OAuth2AutoConfiguration;
import com.ixortalk.autoconfigure.oauth2.feign.OAuth2FeignRequestInterceptor;
import com.ixortalk.autoconfigure.oauth2.feign.ServiceToServiceFeignConfiguration;
import com.ixortalk.user.registration.api.auth.AuthServer;
import feign.Feign;
import feign.Logger;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.feign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;

@Configuration
@Conditional(OAuth2AutoConfiguration.NoAuth0Condition.class)
@Import(ServiceToServiceFeignConfiguration.class)
public class AuthServerConfig {

    @Value("${ixortalk.server.authserver.url}")
    private String authServerUrl;

    @Inject
    private Logger.Level feignLoggerLevel;

    @Inject
    private  Decoder feignDecoder;

    @Inject
    private  Encoder feignEncoder;

    @Inject
    private OAuth2FeignRequestInterceptor oAuth2FeignRequestInterceptor;

    @Bean
    public AuthServer authServer() {
        return Feign.builder()
                .contract(new SpringMvcContract())
                .logLevel(feignLoggerLevel)
                .decoder(feignDecoder)
                .encoder(feignEncoder)
                .requestInterceptor(oAuth2FeignRequestInterceptor)
                .decode404()
                .target(AuthServer.class, authServerUrl);
    }
}
