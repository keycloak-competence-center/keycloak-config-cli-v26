/*-
 * ---license-start
 * keycloak-config-cli
 * ---
 * Copyright (C) 2017 - 2021 adorsys GmbH & Co. KG @ https://adorsys.com
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package de.adorsys.keycloak.config.util;

import org.keycloak.admin.client.CreatedResponseUtil;

import java.net.URI;
import java.util.Map;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * A debug friendly version of {@link org.keycloak.admin.client.CreatedResponseUtil} that exposes the actual server error message.
 *
 * Note: Calls to {{@link DebugFriendlyCreatedResponseUtil#getCreatedId(Response)}} can be replaced
 * with {{@link CreatedResponseUtil#getCreatedId(Response)}} once https://github.com/keycloak/keycloak/issues/34343 is solved.
 */
public class DebugFriendlyCreatedResponseUtil {

    /**
     * Reads the Response object, confirms that it returns a 201 created and parses the ID from the location
     * It always assumes the ID is the last segment of the URI
     *
     * @param response The JAX-RS Response received
     * @return The String ID portion of the URI
     * @throws WebApplicationException if the response is not a 201 Created
     */
    public static String getCreatedId(Response response) throws WebApplicationException {
        URI location = response.getLocation();
        if (!response.getStatusInfo().equals(Response.Status.CREATED)) {
            Response.StatusType statusInfo = response.getStatusInfo();
            String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            String errorMessage = "Create method returned status "
                                  + statusInfo.getReasonPhrase()
                                  + " (Code: " + statusInfo.getStatusCode() + "); "
                                  + "expected status: Created (201).";
            if (MediaType.APPLICATION_JSON.equals(contentType)) {
                // try to add actual server error message to the exception message
                try {
                    var responseBody = response.readEntity(Map.class);
                    if (responseBody != null && responseBody.containsKey("errorMessage")) {
                        errorMessage += " errorMessage: " + responseBody.get("errorMessage");
                    }
                } catch (Exception ignored) {
                    // ignore if we couldn't parse
                }
            }

            throw new WebApplicationException(errorMessage, response);
        }
        if (location == null) {
            return null;
        }
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
