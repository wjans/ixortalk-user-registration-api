package com.ixortalk.user.registration.api.auth;

import com.ixortalk.test.builder.ReflectionInstanceTestBuilder;

import static com.ixortalk.test.util.Randomizer.nextString;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class CreateUserDTOTestBuilder extends ReflectionInstanceTestBuilder<CreateUserDTO> {

    private String username = nextString("zyzo-user-");
    private String firstName = nextString("zyzo-user_firstName-");
    private String lastName = nextString("zyzo-user-lastName-");
    private String langKey = "en";

    private CreateUserDTOTestBuilder() {}

    public static CreateUserDTOTestBuilder aCreateUserDTO() {
        return new CreateUserDTOTestBuilder();
    }

    @Override
    public void setFields(CreateUserDTO instance) {
        setField(instance, "username", username);
        setField(instance, "firstName", firstName);
        setField(instance, "lastName", lastName);
        setField(instance, "langKey", langKey);
    }

    public CreateUserDTOTestBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public CreateUserDTOTestBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public CreateUserDTOTestBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public CreateUserDTOTestBuilder withLangKey(String langKey) {
        this.langKey = langKey;
        return this;
    }
}