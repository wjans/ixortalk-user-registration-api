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
