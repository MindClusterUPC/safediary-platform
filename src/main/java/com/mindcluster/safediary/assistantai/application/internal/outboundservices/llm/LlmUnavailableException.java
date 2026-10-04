package com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm;

/**
 * Thrown when the language model provider cannot produce a valid answer.
 */
public class LlmUnavailableException extends RuntimeException {

    public LlmUnavailableException(String message) {
        super(message);
    }

    public LlmUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
