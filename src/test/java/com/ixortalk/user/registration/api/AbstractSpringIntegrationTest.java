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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.ixortalk.test.oauth2.OAuth2EmbeddedTestServer;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import feign.RequestInterceptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.feign.FeignContext;
import org.springframework.restdocs.JUnitRestDocumentation;
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

import static com.google.common.net.HttpHeaders.*;
import static com.ixortalk.test.oauth2.OAuth2EmbeddedTestServer.CLIENT_ID_ADMIN;
import static com.ixortalk.test.oauth2.OAuth2EmbeddedTestServer.CLIENT_SECRET_ADMIN;
import static com.jayway.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static com.jayway.restassured.config.RestAssuredConfig.config;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.restassured.operation.preprocess.RestAssuredPreprocessors.modifyUris;
import static org.springframework.security.oauth2.client.test.OAuth2ContextSetup.standard;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@SpringBootTest(classes = {UserRegistrationApiApplication.class, OAuth2EmbeddedTestServer.class}, webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
@RunWith(SpringRunner.class)
public abstract class AbstractSpringIntegrationTest implements RestTemplateHolder {

    private static final String HOST_IXORTALK_COM = "www.ixortalk.com";
    private static final String HTTPS = "https";

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

    @Inject
    protected ObjectMapper objectMapper;

    @Inject
    private FeignContext feignContext;

    private RestOperations restTemplate = new RestTemplate();

    protected static UriModifyingOperationPreprocessor staticUris() {
        return modifyUris().scheme(HTTPS).host(HOST_IXORTALK_COM).removePort();
    }

    protected String getOAuth2AccessTokenUri() {
        return "http://localhost:" + port + "" + contextPath + "/oauth/token";
    }

    @Before
    public void restAssured() {
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
        if (restTemplate instanceof OAuth2RestTemplate) {
            feignContext.getContextNames()
                    .stream()
                    .map(feignContextName -> feignContext.getInstance(feignContextName, RequestInterceptor.class))
                    .forEach(requestInterceptor -> setField(requestInterceptor, "oAuth2RestTemplate", restTemplate, OAuth2RestTemplate.class));
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
}
