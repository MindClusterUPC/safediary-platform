package com.mindcluster.safediary.assistantai.domain.model.commands;

/**
 * Sends a user text message to a session. Locale is "es" or "en"; anything else falls back to "es".
 */
public record SendTextMessageCommand(Long sessionId, String content, String locale) {
}
