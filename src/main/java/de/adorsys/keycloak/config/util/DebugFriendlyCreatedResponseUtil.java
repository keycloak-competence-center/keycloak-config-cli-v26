package de.adorsys.keycloak.config.util;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Map;

/**
 * A debug friendly version of {@link org.keycloak.admin.client.CreatedResponseUtil} that exposes the actual server error message.
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
            String errorMessage = "Create method returned status " +
                                  statusInfo.getReasonPhrase() + " (Code: " + statusInfo.getStatusCode() + "); " +
                                  "expected status: Created (201).";
            if (MediaType.APPLICATION_JSON.equals(contentType)) {
                // try to add actual server error message to the exception message
                try {
                    var responseBody = response.readEntity(Map.class);
                    if (responseBody != null && responseBody.containsKey("errorMessage")) {
                        errorMessage += " errorMessage: " + responseBody.get("errorMessage");
                    }
                } catch(Exception ignored) {
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
