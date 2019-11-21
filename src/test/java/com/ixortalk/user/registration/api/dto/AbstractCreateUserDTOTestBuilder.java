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
package com.ixortalk.user.registration.api.dto;

import com.ixortalk.test.builder.ReflectionInstanceTestBuilder;

import static com.ixortalk.test.util.Randomizer.nextString;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class AbstractCreateUserDTOTestBuilder<TYPE extends CreateUserDTO, BUILDER extends AbstractCreateUserDTOTestBuilder<TYPE, BUILDER>> extends ReflectionInstanceTestBuilder<TYPE> {

    private String username = nextString("user-") + "@ixortalk.com";
    private String firstName = nextString("user_firstName-");
    private String lastName = nextString("user-lastName-");
    private String langKey = "en";

    @Override
    public void setFields(TYPE instance) {
        setField(instance, "username", username);
        setField(instance, "firstName", firstName);
        setField(instance, "lastName", lastName);
        setField(instance, "langKey", langKey);
    }

    public BUILDER withUsername(String username) {
        this.username = username;
        return self();
    }

    public BUILDER withFirstName(String firstName) {
        this.firstName = firstName;
        return self();
    }

    public BUILDER withLastName(String lastName) {
        this.lastName = lastName;
        return self();
    }

    public BUILDER withLangKey(String langKey) {
        this.langKey = langKey;
        return self();
    }

    private BUILDER self() {
        return (BUILDER) this;
    }
}
