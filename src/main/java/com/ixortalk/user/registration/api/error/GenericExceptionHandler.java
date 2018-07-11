/**
 * 2016 (c) IxorTalk CVBA
 * All Rights Reserved.
 * <p>
 * NOTICE:  All information contained herein is, and remains
 * the property of IxorTalk CVBA
 * <p>
 * The intellectual and technical concepts contained
 * herein are proprietary to IxorTalk CVBA
 * and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * <p>
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from IxorTalk CVBA.
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.
 */
package com.ixortalk.user.registration.api.error;

import com.ixortalk.user.registration.api.UserRegistrationApiApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static java.util.UUID.randomUUID;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice(basePackageClasses = {UserRegistrationApiApplication.class})
public class GenericExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity handleException(Exception e) {
        String errorUUID = logError(e);
        return new ResponseEntity("Error - " + errorUUID, new HttpHeaders(), BAD_REQUEST);
    }

    private static String logError(Exception e) {
        String errorUUID = randomUUID().toString();
        LOGGER.error("Invalid request - {}: {}", errorUUID, e.getMessage(), e);
        return errorUUID;
    }
}