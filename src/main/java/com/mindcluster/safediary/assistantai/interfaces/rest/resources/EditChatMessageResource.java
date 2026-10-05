package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for editing a user message and regenerating the dialogue response.
 *
 * @param prompt the new user message content
 * @param locale optional language/locale (defaults to "es")
 * @param personality optional Diarito personality: Sol, Luma, Kai or Nara
 */
public record EditChatMessageResource(
        @NotBlank @Size(max = 2000) String prompt,
        String locale,
        String personality
) {
}
