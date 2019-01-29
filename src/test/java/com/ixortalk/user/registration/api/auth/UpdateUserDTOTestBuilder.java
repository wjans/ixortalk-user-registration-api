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

import com.ixortalk.test.builder.ReflectionInstanceTestBuilder;

import static com.ixortalk.test.util.Randomizer.nextString;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class UpdateUserDTOTestBuilder extends ReflectionInstanceTestBuilder<UpdateUserDTO> {

    private String firstName = nextString("updatedFirstName-");
    private String lastName = nextString("updatedLastName-");
    private String langKey = nextString("updatedLang-");

    private UpdateUserDTOTestBuilder() {}

    public static UpdateUserDTOTestBuilder anUpdateUserDTO() {
        return new UpdateUserDTOTestBuilder();
    }

    @Override
    public void setFields(UpdateUserDTO updateUserDTO) {
        setField(updateUserDTO, "firstName", firstName);
        setField(updateUserDTO, "lastName", lastName);
        setField(updateUserDTO, "langKey", langKey);
    }

    public UpdateUserDTOTestBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public UpdateUserDTOTestBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UpdateUserDTOTestBuilder withLangKey(String langKey) {
        this.langKey = langKey;
        return this;
    }
}