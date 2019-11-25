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

import com.auth0.exception.APIException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ixortalk.autoconfigure.oauth2.auth0.mgmt.api.Auth0RuntimeException;
import com.ixortalk.user.registration.api.AbstractSpringIntegrationTest;
import org.junit.After;
import org.junit.Test;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.ixortalk.test.util.Randomizer.nextString;
import static com.ixortalk.user.registration.api.dto.CreateUserWithPasswordDTOTestBuilder.aCreateUserWithPasswordDTO;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.NULL;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

@OAuth2ContextConfiguration(AbstractSpringIntegrationTest.AdminClientCredentialsResourceDetails.class)
@TestPropertySource(properties = {"ixortalk.auth0.domain = ixortalk-test.auth0.com"})
public class Auth0UserRegistrationController_Register_IntegrationAndRestDocTest extends AbstractSpringIntegrationTest {

    private static final FieldDescriptor USERNAME_FIELD_DESCRIPTOR = fieldWithPath("username").type(STRING).description("The username for the user to create (needs to be the users email address)");
    private static final FieldDescriptor PASSWORD_FIELD_DESCRIPTOR = fieldWithPath("password").type(STRING).description("The password for the user to create");
    private static final FieldDescriptor FIRST_NAME_FIELD_DESCRIPTOR = fieldWithPath("firstName").type(STRING).description("The user's first name");
    private static final FieldDescriptor LAST_NAME_FIELD_DESCRIPTOR = fieldWithPath("lastName").type(STRING).description("The user's last name");
    private static final FieldDescriptor LANG_KEY_FIELD_DESCRIPTOR = fieldWithPath("langKey").type(STRING).description("The user's language ('nl', 'fr', 'en', ... )");

    private static final String LOGIN = nextString("testUser-") + "@ixortalk.com";
    private static final String PASSWORD = nextString("password");
    private static final String FIRST_NAME = nextString("firstName-");
    private static final String LAST_NAME = nextString("lastName-");
    private static final String LANG_KEY = "en";
    private static final String AUTH_0_BAD_REQUEST_DESCRIPTION = "auth0 bad reqeust error message";
    private static final String AUTH_0_FORBIDDEN_ERROR_MESSAGE = "auth0 forbidden error message";

    @After
    public void shouldNeverInteractWithAuthServer() {
        authServerWireMockRule.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    public void success() throws JsonProcessingException {

        given()
                .filter(
                        document("auth0/register/ok",
                                preprocessRequest(staticUris(), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        USERNAME_FIELD_DESCRIPTOR,
                                        PASSWORD_FIELD_DESCRIPTOR,
                                        FIRST_NAME_FIELD_DESCRIPTOR,
                                        LAST_NAME_FIELD_DESCRIPTOR,
                                        LANG_KEY_FIELD_DESCRIPTOR
                                )
                        )
                )
                .when()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(
                        aCreateUserWithPasswordDTO()
                                .withUsername(LOGIN)
                                .withPassword(PASSWORD)
                                .withFirstName(FIRST_NAME)
                                .withLastName(LAST_NAME)
                                .withLangKey(LANG_KEY)
                                .build()))
                .post("/")
                .then()
                .statusCode(HTTP_OK);

        verify(auth0Users).createBlockedUser(LOGIN, PASSWORD, FIRST_NAME, LAST_NAME, LANG_KEY, ixorTalkConfigProperties.getUserRegistration().getDefaultRoles());
    }

    @Test
    public void invalidRequestBody() throws JsonProcessingException {

        given()
                .filter(
                        document("auth0/register/invalid-request-body",
                                preprocessRequest(staticUris(), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("username").type(NULL).ignored(),
                                        PASSWORD_FIELD_DESCRIPTOR,
                                        FIRST_NAME_FIELD_DESCRIPTOR,
                                        LAST_NAME_FIELD_DESCRIPTOR,
                                        LANG_KEY_FIELD_DESCRIPTOR
                                )
                        )
                )
                .when()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(
                        aCreateUserWithPasswordDTO()
                                .withUsername(null)
                                .withPassword(PASSWORD)
                                .withFirstName(FIRST_NAME)
                                .withLastName(LAST_NAME)
                                .withLangKey(LANG_KEY)
                                .build()))
                .post("/")
                .then()
                .statusCode(HTTP_BAD_REQUEST);

        verifyZeroInteractions(auth0Users, auth0Roles);
    }

    @Test
    public void auth0ThrowsBadRequestError() throws JsonProcessingException {

        doThrow(new Auth0RuntimeException("", new APIException(AUTH_0_BAD_REQUEST_DESCRIPTION, HTTP_BAD_REQUEST, new RuntimeException("Faked exception"))))
                .when(auth0Users)
                .createBlockedUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyList());

        given()
                .filter(
                        document("auth0/register/bad-request-returned-by-auth0",
                                preprocessRequest(staticUris(), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        USERNAME_FIELD_DESCRIPTOR,
                                        PASSWORD_FIELD_DESCRIPTOR,
                                        FIRST_NAME_FIELD_DESCRIPTOR,
                                        LAST_NAME_FIELD_DESCRIPTOR,
                                        LANG_KEY_FIELD_DESCRIPTOR
                                )
                        )
                )
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(aCreateUserWithPasswordDTO().build()))
                .post("/")
                .then()
                .statusCode(HTTP_BAD_REQUEST)
                .body(containsString(AUTH_0_BAD_REQUEST_DESCRIPTION));
    }

    @Test
    public void auth0ThrowsSomeOtherError() throws JsonProcessingException {

        doThrow(new Auth0RuntimeException("", new APIException(AUTH_0_FORBIDDEN_ERROR_MESSAGE, HTTP_FORBIDDEN, new RuntimeException("Faked exception"))))
                .when(auth0Users)
                .createBlockedUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyList());

        given()
                .filter(
                        document("auth0/register/error-returned-by-auth0",
                                preprocessRequest(staticUris(), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        USERNAME_FIELD_DESCRIPTOR,
                                        PASSWORD_FIELD_DESCRIPTOR,
                                        FIRST_NAME_FIELD_DESCRIPTOR,
                                        LAST_NAME_FIELD_DESCRIPTOR,
                                        LANG_KEY_FIELD_DESCRIPTOR
                                )
                        )
                )
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(aCreateUserWithPasswordDTO().build()))
                .post("/")
                .then()
                .statusCode(HTTP_FORBIDDEN)
                .body(not(containsString(AUTH_0_FORBIDDEN_ERROR_MESSAGE)));
    }

    @Test
    public void anythingButRootPathRequiresAuthentication() throws JsonProcessingException {

        given()
                .when()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(
                        aCreateUserWithPasswordDTO()
                                .withUsername(LOGIN)
                                .withPassword(PASSWORD)
                                .withFirstName(FIRST_NAME)
                                .withLastName(LAST_NAME)
                                .withLangKey(LANG_KEY)
                                .build()))
                .post("/someOtherPath")
                .then()
                .statusCode(HTTP_UNAUTHORIZED);

        verifyZeroInteractions(auth0Users, auth0Roles);
    }
}