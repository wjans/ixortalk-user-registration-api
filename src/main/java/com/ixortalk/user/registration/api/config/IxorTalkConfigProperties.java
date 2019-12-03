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

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@ConfigurationProperties(prefix = "ixortalk")
@Validated
public class IxorTalkConfigProperties {

    @Valid
    private UserRegistration userRegistration = new UserRegistration();

    @Valid
    private ProfilePicture profilePicture = new ProfilePicture();

    public UserRegistration getUserRegistration() {
        return userRegistration;
    }

    public ProfilePicture getProfilePicture() {
        return profilePicture;
    }

    public static class UserRegistration {

        @NotEmpty
        private List<String> defaultRoles = newArrayList();

        public List<String> getDefaultRoles() {
            return defaultRoles;
        }
    }

    public static class ProfilePicture {

        @NotBlank
        private String s3Bucket;

        public String getS3Bucket() {
            return s3Bucket;
        }

        public void setS3Bucket(String s3Bucket) {
            this.s3Bucket = s3Bucket;
        }
    }
}
