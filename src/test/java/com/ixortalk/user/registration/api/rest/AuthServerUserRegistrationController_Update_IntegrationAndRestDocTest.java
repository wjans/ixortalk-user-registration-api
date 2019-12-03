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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixortalk.user.registration.api.AbstractSpringIntegrationTest;
import com.ixortalk.user.registration.api.auth.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;

import javax.inject.Inject;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.google.common.collect.Sets.newHashSet;
import static com.ixortalk.test.oauth2.OAuth2EmbeddedTestServer.USER_NAME;
import static com.ixortalk.test.oauth2.OAuth2TestTokens.*;
import static com.ixortalk.user.registration.api.dto.UpdateUserDTOTestBuilder.anUpdateUserDTO;
import static com.ixortalk.user.registration.api.auth.User.newUser;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static java.net.HttpURLConnection.*;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.commons.lang.math.RandomUtils.nextLong;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.NULL;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@OAuth2ContextConfiguration(AbstractSpringIntegrationTest.AdminClientCredentialsResourceDetails.class)
public class AuthServerUserRegistrationController_Update_IntegrationAndRestDocTest extends AbstractSpringIntegrationTest {

    private static final Set<String> EXISTING_AUTHORITIES = newHashSet("ROLE_EXISTING_1", "ROLE_EXISTING_2");

    private static final String NEW_FIRST_NAME = "new first name";
    private static final String NEW_LAST_NAME = "new last name";
    private static final String NEW_LANG_KEY = "new lang key";
    private static final long EXISTING_USER_S_ID = nextLong();

    @Inject
    private ObjectMapper objectMapper;

    @Before
    public void before() throws JsonProcessingException {
        User existingUser =
                newUser()
                        .withLogin(USER_NAME)
                        .withFirstName("existing first name")
                        .withLastName("existing last name")
                        .withEmail(USER_NAME)
                        .withActivated(true)
                        .withLangKey("existing lang key")
                        .withAuthorities(EXISTING_AUTHORITIES)
                        .build();

        setField(existingUser, "id", EXISTING_USER_S_ID);

        authServerWireMockRule.stubFor(
                get(urlEqualTo("/api/users/" + USER_NAME))
                        .andMatching(jwtTokenWithAuthority("ROLE_ADMIN"))
                        .willReturn(
                                aResponse()
                                        .withStatus(SC_OK)
                                        .withBody(
                                                objectMapper.writeValueAsString(existingUser)
                                        )
                        )
        );
    }

    @After
    public void shouldNeverInteractWithAuth0() {
        verifyZeroInteractions(auth0Users);
    }

    @Test
    public void success() throws JsonProcessingException {
        authServerWireMockRule.stubFor(
                put(urlEqualTo("/api/users"))
                        .andMatching(jwtTokenWithAuthority("ROLE_ADMIN"))
                        .willReturn(aResponse().withStatus(HTTP_OK)));

        User expectedUser =
                newUser()
                        .withLogin(USER_NAME)
                        .withFirstName(NEW_FIRST_NAME)
                        .withLastName(NEW_LAST_NAME)
                        .withEmail(USER_NAME)
                        .withActivated(true)
                        .withLangKey(NEW_LANG_KEY)
                        .withAuthorities(EXISTING_AUTHORITIES)
                        .build();
        setField(expectedUser, "id", EXISTING_USER_S_ID);

        given()
                .filter(
                        document("auth-server/update/ok",
                                preprocessRequest(staticUris(), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("firstName").type(STRING).description("The user's first name"),
                                        fieldWithPath("lastName").type(STRING).description("The user's last name"),
                                        fieldWithPath("langKey").type(STRING).description("The user's language ('nl', 'fr', 'en', ... )")
                                )
                        )
                )
                .auth().preemptive().oauth2(userToken().getValue())
                .when()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(
                        anUpdateUserDTO()
                                .withFirstName(NEW_FIRST_NAME)
                                .withLastName(NEW_LAST_NAME)
                                .withLangKey(NEW_LANG_KEY)
                                .build()))
                .put("/")
                .then()
                .statusCode(HTTP_OK);

        verify(
                putRequestedFor(urlMatching("/api/users"))
                        .andMatching(jwtTokenWithAuthority("ROLE_ADMIN"))
                        .withRequestBody(equalToJson(objectMapper.writeValueAsString(expectedUser))));
    }

    @Test
    public void userNotFound() throws JsonProcessingException {
        authServerWireMockRule.stubFor(
                get(urlEqualTo("/api/users/" + USER_NAME))
                        .andMatching(jwtTokenWithAuthority("ROLE_ADMIN"))
                        .willReturn(
                                aResponse()
                                        .withStatus(SC_NOT_FOUND)));

        given()
                .filter(
                        document("auth-server/update/logged-in-user-not-found",
                                preprocessRequest(staticUris(), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("firstName").type(STRING).description("The user's first name"),
                                        fieldWithPath("lastName").type(STRING).description("The user's last name"),
                                        fieldWithPath("langKey").type(STRING).description("The user's language ('nl', 'fr', 'en', ... )")
                                )
                        )
                )
                .auth().preemptive().oauth2(userToken().getValue())
                .when()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(
                        anUpdateUserDTO()
                                .withFirstName(NEW_FIRST_NAME)
                                .withLastName(NEW_LAST_NAME)
                                .withLangKey(NEW_LANG_KEY)
                                .build()))
                .put("/")
                .then()
                .statusCode(HTTP_BAD_REQUEST);

        verify(0, putRequestedFor(urlMatching("/api/users")));
    }

    @Test
    public void invalidRequestBody() throws JsonProcessingException {
        given()
                .filter(
                        document("auth-server/update/invalid-request-body",
                                preprocessRequest(staticUris(), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("firstName").type(STRING).description("The user's first name"),
                                        fieldWithPath("lastName").type(NULL).description("The user's last name"),
                                        fieldWithPath("langKey").type(STRING).description("The user's language ('nl', 'fr', 'en', ... )")
                                )
                        )
                )
                .auth().preemptive().oauth2(userToken().getValue())
                .when()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(
                        anUpdateUserDTO()
                                .withFirstName(NEW_FIRST_NAME)
                                .withLastName(null)
                                .withLangKey(NEW_LANG_KEY)
                                .build()))
                .put("/")
                .then()
                .statusCode(HTTP_BAD_REQUEST);

        verify(0, putRequestedFor(urlMatching("/api/users")));
    }

    @Test
    public void authServerReturnsBadRequest() throws JsonProcessingException {
        authServerWireMockRule.stubFor(
                put(urlEqualTo("/api/users"))
                        .andMatching(jwtTokenWithAuthority("ROLE_ADMIN"))
                        .willReturn(aResponse().withStatus(HTTP_BAD_REQUEST)));

        given()
                .auth().preemptive().oauth2(userToken().getValue())
                .when()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(
                        anUpdateUserDTO()
                                .withFirstName(NEW_FIRST_NAME)
                                .withLastName(NEW_LAST_NAME)
                                .withLangKey(NEW_LANG_KEY)
                                .build()))
                .put("/")
                .then()
                .statusCode(HTTP_BAD_REQUEST);
    }

    @Test
    public void authServerReturnsInternalServerError() throws JsonProcessingException {
        authServerWireMockRule.stubFor(
                put(urlEqualTo("/api/users"))
                        .andMatching(jwtTokenWithAuthority("ROLE_ADMIN"))
                        .willReturn(aResponse().withStatus(HTTP_INTERNAL_ERROR)));

        given()
                .filter(
                        document("auth-server/update/error-returned-by-authserver",
                                preprocessRequest(staticUris(), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("firstName").type(STRING).description("The user's first name"),
                                        fieldWithPath("lastName").type(STRING).description("The user's last name"),
                                        fieldWithPath("langKey").type(STRING).description("The user's language ('nl', 'fr', 'en', ... )")
                                )
                        )
                )
                .auth().preemptive().oauth2(userToken().getValue())
                .when()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(
                        anUpdateUserDTO()
                                .withFirstName(NEW_FIRST_NAME)
                                .withLastName(NEW_LAST_NAME)
                                .withLangKey(NEW_LANG_KEY)
                                .build()))
                .put("/")
                .then()
                .statusCode(HTTP_INTERNAL_ERROR);
    }

    @Test
    public void noLoggedInUser() throws JsonProcessingException {
        given()
                .filter(
                        document("auth-server/update/no-logged-in-user",
                                preprocessRequest(staticUris(), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("firstName").type(STRING).description("The user's first name"),
                                        fieldWithPath("lastName").type(STRING).description("The user's last name"),
                                        fieldWithPath("langKey").type(STRING).description("The user's language ('nl', 'fr', 'en', ... )")
                                )
                        )
                )
                .when()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(
                        anUpdateUserDTO()
                                .withFirstName(NEW_FIRST_NAME)
                                .withLastName(NEW_LAST_NAME)
                                .withLangKey(NEW_LANG_KEY)
                                .build()))
                .put("/")
                .then()
                .statusCode(HTTP_UNAUTHORIZED);

        verify(0, putRequestedFor(urlMatching("/api/users")));
    }
}