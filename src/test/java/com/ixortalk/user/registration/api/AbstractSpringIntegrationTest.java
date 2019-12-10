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
package com.ixortalk.user.registration.api;

import com.auth0.client.mgmt.ManagementAPI;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import com.ixortalk.autoconfigure.oauth2.auth0.mgmt.api.Auth0ManagementAPI;
import com.ixortalk.autoconfigure.oauth2.auth0.mgmt.api.Auth0Roles;
import com.ixortalk.autoconfigure.oauth2.auth0.mgmt.api.Auth0Users;
import com.ixortalk.autoconfigure.oauth2.feign.OAuth2FeignRequestInterceptor;
import com.ixortalk.aws.s3.library.config.AwsS3Template;
import com.ixortalk.test.oauth2.OAuth2EmbeddedTestServer;
import com.ixortalk.user.registration.api.config.IxorTalkConfigProperties;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.restassured.operation.preprocess.UriModifyingOperationPreprocessor;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.test.OAuth2ContextSetup;
import org.springframework.security.oauth2.client.test.RestTemplateHolder;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.net.HttpHeaders.X_FORWARDED_HOST;
import static com.google.common.net.HttpHeaders.X_FORWARDED_PORT;
import static com.google.common.net.HttpHeaders.X_FORWARDED_PROTO;
import static com.ixortalk.test.oauth2.OAuth2EmbeddedTestServer.CLIENT_ID_ADMIN;
import static com.ixortalk.test.oauth2.OAuth2EmbeddedTestServer.CLIENT_SECRET_ADMIN;
import static com.jayway.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static com.jayway.restassured.config.RestAssuredConfig.config;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.restassured.operation.preprocess.RestAssuredPreprocessors.modifyUris;
import static org.springframework.security.jwt.JwtHelper.decode;
import static org.springframework.security.oauth2.client.test.OAuth2ContextSetup.standard;
import static org.springframework.security.oauth2.provider.token.AccessTokenConverter.AUTHORITIES;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@SpringBootTest(classes = {UserRegistrationApiApplication.class, OAuth2EmbeddedTestServer.class}, webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
@RunWith(SpringRunner.class)
public abstract class AbstractSpringIntegrationTest implements RestTemplateHolder {

    private static final String HOST_IXORTALK_COM = "www.ixortalk.com";
    private static final String HTTPS = "https";

    public static final HeaderDescriptor AUTHORIZATION_TOKEN_HEADER = headerWithName("Authorization").description("The bearer token needed to authorize this request.");

    @Rule
    public WireMockRule authServerWireMockRule = new WireMockRule(65444);

    @Rule
    public OAuth2ContextSetup context = standard(this);

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    @LocalServerPort
    protected int port;

    @Value("${server.context-path}")
    protected String contextPath;

    @MockBean
    protected Auth0Users auth0Users;

    @MockBean
    protected Auth0Roles auth0Roles;

    @MockBean
    private ManagementAPI managementAPI;

    @MockBean
    protected Auth0ManagementAPI auth0ManagementAPI;

    @MockBean
    protected AwsS3Template awsS3Template;

    @Inject
    protected ObjectMapper objectMapper;

    @Inject
    protected IxorTalkConfigProperties ixorTalkConfigProperties;

    @Autowired(required = false)
    private OAuth2FeignRequestInterceptor oAuth2FeignRequestInterceptor;

    private RestOperations restTemplate = new RestTemplate();

    protected static UriModifyingOperationPreprocessor staticUris() {
        return modifyUris().scheme(HTTPS).host(HOST_IXORTALK_COM).removePort();
    }

    protected String getOAuth2AccessTokenUri() {
        return "http://localhost:" + port + "" + contextPath + "/oauth/token";
    }

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = contextPath;
        RestAssured.config = config().objectMapperConfig(objectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> objectMapper));
        RestAssured.requestSpecification =
                new RequestSpecBuilder()
                        .addFilter(documentationConfiguration(this.restDocumentation))
                        .addHeader(X_FORWARDED_PROTO, HTTPS)
                        .addHeader(X_FORWARDED_HOST, HOST_IXORTALK_COM)
                        .addHeader(X_FORWARDED_PORT, "")
                        .build();
    }

    public RestOperations getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestOperations restTemplate) {
        this.restTemplate = restTemplate;
        if (oAuth2FeignRequestInterceptor != null && restTemplate instanceof OAuth2RestTemplate) {
            setField(oAuth2FeignRequestInterceptor, "oAuth2RestTemplate", restTemplate, OAuth2RestTemplate.class);
        }
    }

    public static class AdminClientCredentialsResourceDetails extends ClientCredentialsResourceDetails {
        public AdminClientCredentialsResourceDetails(Object object) {
            AbstractSpringIntegrationTest abstractSpringIntegrationTest = (AbstractSpringIntegrationTest) object;
            setAccessTokenUri(abstractSpringIntegrationTest.getOAuth2AccessTokenUri());
            setClientId(CLIENT_ID_ADMIN);
            setClientSecret(CLIENT_SECRET_ADMIN);
        }
    }

    protected static ValueMatcher<Request> jwtTokenWithAuthority(String role) {
        return request -> {
            try {
                String authorizationHeader = request.getHeader(AUTHORIZATION);
                String bearerToken = substringAfter(authorizationHeader, org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor.BEARER + " ");
                return MatchResult.of(((List<String>) new ObjectMapper().readValue(decode(bearerToken).getClaims(), Map.class).get(AUTHORITIES)).contains(role));
            } catch (IOException e) {
                throw new RuntimeException("Could not deserialize claims: " + e.getMessage(), e);
            }
        };
    }
}
