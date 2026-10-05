package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Chat request with the exact contract used by SafeDiary-Mobile (PromptRequestDto).
 *
 * @param personality optional Diarito personality: Sol, Luma, Kai or Nara
 */
public record AssistantChatRequestResource(@NotBlank @Size(max = 2000) String prompt, String conversationId,
                                           String locale, String personality) {
}
