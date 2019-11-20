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

import com.ixortalk.autoconfigure.oauth2.OAuth2AutoConfiguration;
import com.ixortalk.user.registration.api.auth.AuthServer;
import com.ixortalk.user.registration.api.auth.CreateUserDTO;
import com.ixortalk.user.registration.api.auth.UpdateUserDTO;
import com.ixortalk.user.registration.api.auth.User;
import com.ixortalk.user.registration.api.config.IxorTalkConfigProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;
import java.security.Principal;

import static com.google.common.collect.Sets.newHashSet;
import static com.ixortalk.user.registration.api.auth.User.newUser;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Conditional(OAuth2AutoConfiguration.NoAuth0Condition.class)
public class AuthServerUserRegistrationController {

    @Inject
    private AuthServer authServer;

    @Inject
    private IxorTalkConfigProperties ixorTalkConfigProperties;

    @PostMapping(path = "/", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@Valid @RequestBody CreateUserDTO createUserDTO) {
        authServer.createUser(
                newUser()
                        .withLogin(createUserDTO.getUsername())
                        .withFirstName(createUserDTO.getFirstName())
                        .withLastName(createUserDTO.getLastName())
                        .withEmail(createUserDTO.getUsername())
                        .withActivated(true)
                        .withLangKey(createUserDTO.getLangKey())
                        .withAuthorities(newHashSet(ixorTalkConfigProperties.getUserRegistration().getDefaultRoles()))
                        .build()
        );
        return ok().build();
    }

    @PutMapping(path = "/", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@Valid @RequestBody UpdateUserDTO updateUserDTO, Principal principal) {
        User exitingUser = authServer.getUser(principal.getName());
        exitingUser.updateFirstName(updateUserDTO.getFirstName());
        exitingUser.updateLastName(updateUserDTO.getLastName());
        exitingUser.updateLangKey(updateUserDTO.getLangKey());
        authServer.updateUser(exitingUser);
        return ok().build();
    }
}
