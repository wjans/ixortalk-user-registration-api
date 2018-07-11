package com.ixortalk.user.registration.api.auth;

import com.ixortalk.user.registration.api.config.FeignConfiguration;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@FeignClient(name = "authserver",url = "${ixortalk.server.authserver.url}", configuration = FeignConfiguration.class, decode404 = true)
public interface AuthServer {

    @RequestMapping(method = POST, path = "/api/users", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    User createUser(User user);
}
