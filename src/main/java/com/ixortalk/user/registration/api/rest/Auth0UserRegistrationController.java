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
import com.ixortalk.autoconfigure.oauth2.OAuth2AutoConfiguration;
import com.ixortalk.autoconfigure.oauth2.auth0.mgmt.api.Auth0ManagementAPI;
import com.ixortalk.autoconfigure.oauth2.auth0.mgmt.api.Auth0RuntimeException;
import com.ixortalk.autoconfigure.oauth2.auth0.mgmt.api.Auth0Users;
import com.ixortalk.autoconfigure.oauth2.claims.ClaimsProvider;
import com.ixortalk.aws.s3.library.config.AwsS3Template;
import com.ixortalk.user.registration.api.config.IxorTalkConfigProperties;
import com.ixortalk.user.registration.api.dto.CreateUserWithPasswordDTO;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URL;

import static com.amazonaws.HttpMethod.PUT;
import static com.amazonaws.services.s3.Headers.S3_CANNED_ACL;
import static com.amazonaws.services.s3.model.CannedAccessControlList.PublicRead;
import static com.auth0.jwt.impl.PublicClaims.SUBJECT;
import static java.util.Collections.singletonMap;
import static java.util.UUID.randomUUID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Conditional(OAuth2AutoConfiguration.Auth0Condition.class)
public class Auth0UserRegistrationController {

    @Inject
    private Auth0Users auth0Users;

    @Inject
    private Auth0ManagementAPI auth0ManagementAPI;

    @Inject
    private IxorTalkConfigProperties ixorTalkConfigProperties;

    @Inject
    private ClaimsProvider claimsProvider;

    @Inject
    private AwsS3Template awsS3Template;

    @PostMapping(path = "/", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@Valid @RequestBody CreateUserWithPasswordDTO createUserWithPasswordDTO) {
        auth0Users.createBlockedUser(
                createUserWithPasswordDTO.getUsername(),
                createUserWithPasswordDTO.getPassword(),
                createUserWithPasswordDTO.getFirstName(),
                createUserWithPasswordDTO.getLastName(),
                createUserWithPasswordDTO.getLangKey(),
                ixorTalkConfigProperties.getUserRegistration().getDefaultRoles());
        return ok().build();
    }

    @PostMapping(path = "/generate-profile-picture-url", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> generateProfilePictureUrl() {

        String generatedProfilePictureKey = randomUUID().toString();
        auth0ManagementAPI
                .updateProfilePicture(
                        claimsProvider.getClaim(SUBJECT).orElseThrow(() -> new Auth0RuntimeException("No Auth0 subject claim present")),
                        awsS3Template.getObjectUrl(ixorTalkConfigProperties.getProfilePicture().getS3Bucket(), generatedProfilePictureKey).toString());

        return ok(singletonMap("url", generatePresignedUrl(generatedProfilePictureKey).toString()));
    }

    private URL generatePresignedUrl(String key) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(ixorTalkConfigProperties.getProfilePicture().getS3Bucket(), key)
                        .withMethod(PUT);
        generatePresignedUrlRequest.addRequestParameter(S3_CANNED_ACL, PublicRead.toString());
        return awsS3Template.generatePresignedUrl(generatePresignedUrlRequest);
    }
}
