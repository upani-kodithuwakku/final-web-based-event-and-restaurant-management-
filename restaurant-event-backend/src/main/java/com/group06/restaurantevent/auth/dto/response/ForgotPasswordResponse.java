package com.group06.restaurantevent.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForgotPasswordResponse {
    /** Always the same message, so the API never reveals which emails are registered. */
    private String message;
    /**
     * The reset link, returned only when app.auth.expose-reset-link=true (local demo, no email server).
     * In every case the link is also written to the backend log.
     */
    private String resetLink;
}
