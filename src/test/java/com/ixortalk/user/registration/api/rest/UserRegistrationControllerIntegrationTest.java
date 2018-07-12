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
package com.ixortalk.user.registration.api.rest;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ixortalk.user.registration.api.AbstractSpringIntegrationTest;
import com.ixortalk.user.registration.api.auth.User;
import com.ixortalk.user.registration.api.config.IxorTalkConfigProperties;
import org.junit.Test;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.ixortalk.test.util.Randomizer.nextString;
import static com.ixortalk.user.registration.api.auth.CreateUserDTOTestBuilder.aCreateUserDTO;
import static com.ixortalk.user.registration.api.auth.User.newUser;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.assertj.core.util.Sets.newHashSet;

@OAuth2ContextConfiguration(AbstractSpringIntegrationTest.AdminClientCredentialsResourceDetails.class)
public class UserRegistrationControllerIntegrationTest extends AbstractSpringIntegrationTest {

    public static final String LOGIN = nextString("testUser-") + "@ixortalk.com";
    public static final String FIRST_NAME = nextString("firstName-");
    public static final String LAST_NAME = nextString("lastName-");
    public static final String LANG_KEY = "en";

    @Inject
    private IxorTalkConfigProperties ixorTalkConfigProperties;

    @Test
    public void success() throws JsonProcessingException {
        stubFor(
                post(urlEqualTo("/api/users"))
                        .withHeader("Authorization", containing("Bearer"))
                        .willReturn(aResponse().withStatus(HTTP_OK)));

        User expectedUser =
                newUser()
                        .withLogin(LOGIN)
                        .withFirstName(FIRST_NAME)
                        .withLastName(LAST_NAME)
                        .withEmail(LOGIN)
                        .withActivated(true)
                        .withLangKey(LANG_KEY)
                        .withAuthorities(newHashSet(ixorTalkConfigProperties.getUserRegistration().getDefaultRoles()))
                        .build();

        given()
                .when()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(
                        aCreateUserDTO()
                                .withUsername(LOGIN)
                                .withFirstName(FIRST_NAME)
                                .withLastName(LAST_NAME)
                                .withLangKey(LANG_KEY)
                                .build()))
                .post("/")
                .then()
                .statusCode(HTTP_OK);

        verify(
                postRequestedFor(urlMatching("/api/users"))
                        .withHeader("Authorization", containing("Bearer"))
                        .withRequestBody(equalToJson(objectMapper.writeValueAsString(expectedUser))));
    }

    @Test
    public void invalidRequestBody() throws JsonProcessingException {
        given()
                .when()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(
                        aCreateUserDTO()
                                .withUsername(null)
                                .withFirstName(FIRST_NAME)
                                .withLastName(LAST_NAME)
                                .withLangKey(LANG_KEY)
                                .build()))
                .post("/")
                .then()
                .statusCode(HTTP_BAD_REQUEST);

        verify(0, postRequestedFor(urlMatching("/api/users")));
    }

    @Test
    public void authServerReturnsBadRequest() throws JsonProcessingException {
        stubFor(
                post(urlEqualTo("/api/users"))
                        .withHeader("Authorization", containing("Bearer"))
                        .willReturn(aResponse().withStatus(HTTP_BAD_REQUEST)));

        given()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(aCreateUserDTO().build()))
                .post("/")
                .then()
                .statusCode(HTTP_BAD_REQUEST);
    }

    @Test
    public void authServerReturnsInternalServerError() throws JsonProcessingException {
        stubFor(
                post(urlEqualTo("/api/users"))
                        .withHeader("Authorization", containing("Bearer"))
                        .willReturn(aResponse().withStatus(HTTP_INTERNAL_ERROR)));

        given()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(aCreateUserDTO().build()))
                .post("/")
                .then()
                .statusCode(HTTP_INTERNAL_ERROR);
    }

    @Test
    public void anythingButRootPathRequiresAuthentication() throws JsonProcessingException {
        given()
                .when()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(
                        aCreateUserDTO()
                                .withUsername(LOGIN)
                                .withFirstName(FIRST_NAME)
                                .withLastName(LAST_NAME)
                                .withLangKey(LANG_KEY)
                                .build()))
                .post("/someOtherPath")
                .then()
                .statusCode(HTTP_UNAUTHORIZED);
    }
}