package com.ixortalk.user.registration.api.config;

import java.util.List;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import static com.google.common.collect.Lists.newArrayList;

@ConfigurationProperties(prefix = "ixortalk")
@Validated
public class IxorTalkConfigProperties {

    @Valid
    private UserRegistration userRegistration = new UserRegistration();

    public UserRegistration getUserRegistration() {
        return userRegistration;
    }

    public static class UserRegistration {

        @NotEmpty
        private List<String> defaultRoles = newArrayList();

        public List<String> getDefaultRoles() {
            return defaultRoles;
        }
    }
}
