package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to send a text message. Locale is optional ("es" or "en").
 */
public record SendTextMessageResource(@NotBlank @Size(max = 2000) String content, String locale) {
}
