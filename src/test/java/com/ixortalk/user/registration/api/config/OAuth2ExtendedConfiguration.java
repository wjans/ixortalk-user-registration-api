/**
 * 2016 (c) IxorTalk CVBA
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of IxorTalk CVBA
 *
 * The intellectual and technical concepts contained
 * herein are proprietary to IxorTalk CVBA
 * and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 *
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from IxorTalk CVBA.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.
 */
package com.ixortalk.user.registration.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import javax.inject.Inject;

import static com.auth0.jwt.impl.PublicClaims.SUBJECT;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonMap;

@Configuration
public class OAuth2ExtendedConfiguration {

    @Configuration
    public class ExtendedAuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

        @Inject
        private JwtAccessTokenConverter accessTokenConverter;

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
            endpoints
                    .tokenEnhancer(tokenEnhancerChain());
        }

        @Bean
        public TokenEnhancerChain tokenEnhancerChain() {
            TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
            tokenEnhancerChain.setTokenEnhancers(newArrayList(additionalInfoTokenEnhancer(), accessTokenConverter));
            return tokenEnhancerChain;
        }

        private TokenEnhancer additionalInfoTokenEnhancer() {
            return (accessToken, authentication) -> {
                ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(singletonMap(SUBJECT, authentication.getName()));
                return accessToken;
            };
        }
    }
}
