package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

/**
 * Optional request body for regenerating the last assistant reply.
 *
 * @param locale optional language/locale (defaults to "es")
 * @param personality optional Diarito personality: Sol, Luma, Kai or Nara
 */
public record RegenerateChatMessageResource(String locale, String personality) {
}
