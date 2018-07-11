/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016-present IxorTalk CVBA
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
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
import com.ixortalk.user.registration.api.AbstractRestDocTest;
import com.ixortalk.user.registration.api.AbstractSpringIntegrationTest;
import com.ixortalk.user.registration.api.config.IxorTalkConfigProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.ixortalk.user.registration.api.auth.CreateUserDTOTestBuilder.aCreateUserDTO;
import static com.ixortalk.user.registration.api.rest.UserRegistrationControllerIntegrationTest.FIRST_NAME;
import static com.ixortalk.user.registration.api.rest.UserRegistrationControllerIntegrationTest.LANG_KEY;
import static com.ixortalk.user.registration.api.rest.UserRegistrationControllerIntegrationTest.LAST_NAME;
import static com.ixortalk.user.registration.api.rest.UserRegistrationControllerIntegrationTest.LOGIN;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.NULL;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

@OAuth2ContextConfiguration(AbstractSpringIntegrationTest.AdminClientCredentialsResourceDetails.class)
public class UserRegistrationControllerRestDocTest extends AbstractRestDocTest {

    @Inject
    private IxorTalkConfigProperties ixorTalkConfigProperties;

    @Before
    public void before() {
        stubFor(
                post(urlEqualTo("/api/users"))
                        .withHeader("Authorization", containing("Bearer"))
                        .willReturn(aResponse().withStatus(HTTP_OK)));
    }

    @Test
    public void success() throws JsonProcessingException {
        given(this.spec)
                .filter(
                        document("register/ok",
                                preprocessRequest(staticUris(), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("username").type(STRING).description("The username for the user to create (needs to be the users email address)"),
                                        fieldWithPath("firstName").type(STRING).description("The user's first name"),
                                        fieldWithPath("lastName").type(STRING).description("The user's last name"),
                                        fieldWithPath("langKey").type(STRING).description("The user's language ('nl', 'fr', 'en', ... )")
                                )
                        )
                )
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
    }

    @Test
    public void invalidRequestBody() throws JsonProcessingException {
        given(this.spec)
                .filter(
                        document("register/invalid-request-body",
                                preprocessRequest(staticUris(), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("username").type(NULL).description("The username for the user to create (needs to be the users email address)"),
                                        fieldWithPath("firstName").type(STRING).description("The user's first name"),
                                        fieldWithPath("lastName").type(STRING).description("The user's last name"),
                                        fieldWithPath("langKey").type(STRING).description("The user's language ('nl', 'fr', 'en', ... )")
                                )
                        )
                )
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
    public void errorReturnedByAuthServer() throws JsonProcessingException {
        stubFor(
                post(urlEqualTo("/api/users"))
                        .withHeader("Authorization", containing("Bearer"))
                        .willReturn(aResponse().withStatus(HTTP_BAD_REQUEST)));

        given(this.spec)
                .filter(
                        document("register/error-returned-by-authserver",
                                preprocessRequest(staticUris(), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("username").type(STRING).description("The username for the user to create (needs to be the users email address)"),
                                        fieldWithPath("firstName").type(STRING).description("The user's first name"),
                                        fieldWithPath("lastName").type(STRING).description("The user's last name"),
                                        fieldWithPath("langKey").type(STRING).description("The user's language ('nl', 'fr', 'en', ... )")
                                )
                        )
                )
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(aCreateUserDTO().build()))
                .post("/")
                .then()
                .statusCode(HTTP_BAD_REQUEST);
    }
}