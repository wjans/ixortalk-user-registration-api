/**
 *
 *  2016 (c) IxorTalk CVBA
 *  All Rights Reserved.
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
package com.ixortalk.user.registration.api.auth;

import java.util.Set;

import com.ixortalk.util.InstanceBuilder;

public class User {

    private String login;
    private String firstName;
    private String lastName;
    private String email;
    private boolean activated;
    private String langKey;
    private Set<String> authorities;

    private User() {
    }

    public String getLogin() {
        return login;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActivated() {
        return activated;
    }

    public String getLangKey() {
        return langKey;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public static Builder newUser() {
        return new Builder();
    }

    public static class Builder extends InstanceBuilder<User> {

        public Builder() {
            super();
        }

        @Override
        protected User createInstance() {
            return new User();
        }

        public Builder withLogin(String login) {
            instance().login = login;
            return this;
        }

        public Builder withFirstName(String firstName) {
            instance().firstName = firstName;
            return this;
        }

        public Builder withLastName(String lastName) {
            instance().lastName = lastName;
            return this;
        }

        public Builder withEmail(String email) {
            instance().email = email;
            return this;
        }

        public Builder withActivated(boolean activated) {
            instance().activated = activated;
            return this;
        }

        public Builder withLangKey(String langKey) {
            instance().langKey = langKey;
            return this;
        }

        public Builder withAuthorities(Set<String> authorities) {
            instance().authorities = authorities;
            return this;
        }
    }
}
