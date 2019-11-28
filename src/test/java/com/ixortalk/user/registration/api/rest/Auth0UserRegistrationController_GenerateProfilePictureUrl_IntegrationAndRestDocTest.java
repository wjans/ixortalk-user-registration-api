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

import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.ixortalk.user.registration.api.AbstractSpringIntegrationTest;
import com.jayway.restassured.path.json.JsonPath;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.net.MalformedURLException;
import java.net.URL;

import static com.amazonaws.HttpMethod.PUT;
import static com.amazonaws.services.s3.Headers.S3_CANNED_ACL;
import static com.amazonaws.services.s3.model.CannedAccessControlList.PublicRead;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.ixortalk.test.oauth2.OAuth2EmbeddedTestServer.USER_NAME;
import static com.ixortalk.test.oauth2.OAuth2TestTokens.userToken;
import static com.ixortalk.user.registration.api.TestConstants.PROFILE_PICTURE_BUCKET;
import static com.jayway.restassured.RestAssured.given;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

@OAuth2ContextConfiguration(AbstractSpringIntegrationTest.AdminClientCredentialsResourceDetails.class)
@TestPropertySource(properties = {"ixortalk.auth0.domain = ixortalk-test.auth0.com"})
public class Auth0UserRegistrationController_GenerateProfilePictureUrl_IntegrationAndRestDocTest extends AbstractSpringIntegrationTest {

    private static final String EXPECTED_PRESIGNED_URL_STRING = "https://profile-pictures-bucket.s3.amazonaws.com/key?authHeader=abcd";
    private static final String EXPECTED_OBJECT_URL_STRING = "https://profile-pictures-bucket.s3.amazonaws.com/key";

    private URL expectedPresignedUrl, expectedObjectUrl;

    @Before
    public void before() throws MalformedURLException {
        expectedPresignedUrl = new URL(EXPECTED_PRESIGNED_URL_STRING);
        expectedObjectUrl = new URL(EXPECTED_OBJECT_URL_STRING);
    }

    @After
    public void shouldNeverInteractWithAuthServer() {
        authServerWireMockRule.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    public void success() {

        when(awsS3Template.generatePresignedUrl(any())).thenReturn(expectedPresignedUrl);
        when(awsS3Template.getObjectUrl(eq(PROFILE_PICTURE_BUCKET.configuredValue()), anyString())).thenReturn(expectedObjectUrl);

        JsonPath jsonPath =
                given()
                        .auth().preemptive().oauth2(userToken().getValue())
                        .filter(
                                document("auth0/generate-profile-picture-url/ok",
                                        preprocessRequest(staticUris(), prettyPrint()),
                                        preprocessResponse(prettyPrint()),
                                        requestHeaders(AUTHORIZATION_TOKEN_HEADER),
                                        responseFields(fieldWithPath("url").type(STRING).description("The presigned URL to be used to upload (`PUT`) the profile picture to.  This link is valid for a configurable amount of time (1 hour by default)."))
                                )
                        )
                        .when()
                        .post("/generate-profile-picture-url")
                        .then()
                        .statusCode(HTTP_OK)
                        .extract().jsonPath();

        assertThat(jsonPath.getString("url")).isEqualTo(EXPECTED_PRESIGNED_URL_STRING);

        ArgumentCaptor<GeneratePresignedUrlRequest> presignedUrlRequestCaptor = forClass(GeneratePresignedUrlRequest.class);
        ArgumentCaptor<String> keyCaptor = forClass(String.class);

        verify(awsS3Template).generatePresignedUrl(presignedUrlRequestCaptor.capture());
        verify(awsS3Template).getObjectUrl(eq(PROFILE_PICTURE_BUCKET.configuredValue()), keyCaptor.capture());

        assertThat(presignedUrlRequestCaptor.getValue()).isEqualToComparingFieldByFieldRecursively(expectedGeneratePresignedUrlRequest(keyCaptor.getValue()));
        verify(auth0ManagementAPI).updateProfilePicture(USER_NAME, expectedObjectUrl.toString());
    }

    @Test
    public void notLoggedIn() {

        when(awsS3Template.generatePresignedUrl(any())).thenReturn(expectedPresignedUrl);
        when(awsS3Template.getObjectUrl(eq(PROFILE_PICTURE_BUCKET.configuredValue()), anyString())).thenReturn(expectedObjectUrl);

        given()
                .filter(
                        document("auth0/generate-profile-picture-url/not-logged-in",
                                preprocessRequest(staticUris(), prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                )
                .when()
                .post("/generate-profile-picture-url")
                .then()
                .statusCode(HTTP_UNAUTHORIZED);

        verifyZeroInteractions(auth0ManagementAPI, awsS3Template);
    }

    private static GeneratePresignedUrlRequest expectedGeneratePresignedUrlRequest(String key) {
        GeneratePresignedUrlRequest expected =
                new GeneratePresignedUrlRequest(PROFILE_PICTURE_BUCKET.configuredValue(), key)
                        .withMethod(PUT);
        expected.addRequestParameter(S3_CANNED_ACL, PublicRead.toString());
        return expected;
    }
}